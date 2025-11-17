/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.pillar2submissionapi.controllers.actions

import com.google.inject.{Inject, Singleton}
import play.api.Logging
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Organisation}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames}
import uk.gov.hmrc.pillar2submissionapi.config.AppConfig
import uk.gov.hmrc.pillar2submissionapi.controllers.error.*
import uk.gov.hmrc.pillar2submissionapi.models.requests.IdentifierRequest
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthenticatedIdentifierAction @Inject() (
  override val authConnector: AuthConnector,
  val config:                 AppConfig
)(using val executionContext: ExecutionContext)
    extends IdentifierAction
    with AuthorisedFunctions
    with Logging {

  import AuthenticatedIdentifierAction.*
  private def getPillar2Id(enrolments: Enrolments): Option[String] =
    for {
      pillar2Enrolment <- enrolments.getEnrolment(HMRC_PILLAR2_ORG_KEY)
      identifier       <- pillar2Enrolment.getIdentifier(ENROLMENT_IDENTIFIER)
    } yield identifier.value

  private def processPillar2Enrolment[A](
    request:    RequestWithPillar2Id[A],
    enrolments: Enrolments,
    internalId: String,
    groupId:    String,
    providerId: String
  ): Future[IdentifierRequest[A]] = getPillar2Id(
    enrolments
  ) match {
    case Some(pillar2Id) =>
      if request.pillar2Id != pillar2Id then throw IncorrectHeaderValue
      else
        Future.successful(
          IdentifierRequest(
            request = request,
            userId = internalId,
            groupId = Some(groupId),
            clientPillar2Id = pillar2Id,
            userIdForEnrolment = providerId
          )
        )
    case None =>
      logger.warn(s"Invalid Pillar2 enrolment - userId: $internalId")
      Future.failed(InvalidEnrolment)
  }

  override protected def transform[A](request: RequestWithPillar2Id[A]): Future[IdentifierRequest[A]] = {
    given hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    if !request.headers.get(HeaderNames.authorisation).exists(_.trim.nonEmpty) then throw MissingCredentials
    else {
      val retrievals = Retrievals.internalId and Retrievals.groupIdentifier and
        Retrievals.allEnrolments and Retrievals.affinityGroup and
        Retrievals.credentialRole and Retrievals.credentials

      authorised(AuthProviders(GovernmentGateway) and ConfidenceLevel.L50)
        .retrieve(retrievals) {
          case Some(internalId) ~ Some(groupId) ~ enrolments ~ Some(Organisation) ~ None ~ Some(credentials) if config.allowTestUsers =>
            processPillar2Enrolment(request = request, enrolments = enrolments, internalId = internalId, groupId = groupId, credentials.providerId)
          case Some(internalId) ~ Some(groupId) ~ enrolments ~ Some(Organisation) ~ Some(User) ~ Some(credentials) =>
            processPillar2Enrolment(request = request, enrolments = enrolments, internalId = internalId, groupId = groupId, credentials.providerId)
          case Some(_) ~ Some(_) ~ _ ~ Some(Agent) ~ Some(User) ~ Some(_) =>
            agentAuth[A](request, request.pillar2Id)
          case Some(_) ~ Some(_) ~ _ ~ Some(Agent) ~ None ~ Some(_) if config.allowTestUsers =>
            agentAuth[A](request, request.pillar2Id)
          case _ =>
            logger.warn("User is not valid for this API")
            Future.failed(ForbiddenError)
        } recoverWith { case e: AuthorisationException =>
        logger.warn(s"Authorization failed", e)
        Future.failed(InvalidCredentials)
      }
    }
  }

  private def agentAuth[A](request: RequestWithPillar2Id[A], pillar2Id: String)(using hc: HeaderCarrier): Future[IdentifierRequest[A]] = {
    val retrievals = Retrievals.internalId and Retrievals.groupIdentifier and
      Retrievals.allEnrolments and Retrievals.affinityGroup and
      Retrievals.credentialRole and Retrievals.credentials
    authorised(
      AuthProviders(GovernmentGateway) and
        AffinityGroup.Agent and
        Enrolment(HMRC_PILLAR2_ORG_KEY)
          .withIdentifier(ENROLMENT_IDENTIFIER, pillar2Id)
          .withDelegatedAuthRule(DELEGATED_AUTH_RULE)
    ).retrieve(retrievals) {
      case Some(internalId) ~ Some(groupId) ~ _ ~ Some(_) ~ _ ~ Some(credentials) =>
        logger.info(
          s"EnrolmentAuthIdentifierAction - Successfully retrieved Agent enrolment"
        )
        Future.successful(
          IdentifierRequest(
            request,
            internalId,
            Some(groupId),
            clientPillar2Id = pillar2Id,
            userIdForEnrolment = credentials.providerId
          )
        )
      case _ => Future.failed(ForbiddenError)
    }
  }
}

object AuthenticatedIdentifierAction {
  val HMRC_PILLAR2_ORG_KEY = "HMRC-PILLAR2-ORG"
  val ENROLMENT_IDENTIFIER = "PLRID"
  val DELEGATED_AUTH_RULE  = "pillar2-auth"
}
