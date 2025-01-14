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
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Organisation}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.controllers.error.AuthenticationError
import uk.gov.hmrc.pillar2submissionapi.models.requests.IdentifierRequest
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthenticatedIdentifierAction @Inject() (
  override val authConnector:    AuthConnector,
  val parser:                    BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends IdentifierAction
    with AuthorisedFunctions
    with Logging {

  import AuthenticatedIdentifierAction._
  private def getPillar2Id(enrolments: Enrolments): Option[String] =
    for {
      pillar2Enrolment <- enrolments.getEnrolment(HMRC_PILLAR2_ORG_KEY)
      identifier       <- pillar2Enrolment.getIdentifier(ENROLMENT_IDENTIFIER)
    } yield identifier.value

  override protected def transform[A](request: Request[A]): Future[IdentifierRequest[A]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    val retrievals = Retrievals.internalId and Retrievals.groupIdentifier and
      Retrievals.allEnrolments and Retrievals.affinityGroup and
      Retrievals.credentialRole and Retrievals.credentials

    authorised(AuthProviders(GovernmentGateway) and ConfidenceLevel.L50)
      .retrieve(retrievals) {
        case Some(internalId) ~ Some(groupId) ~ enrolments ~ Some(Organisation) ~ Some(User) ~ Some(credentials) =>
          getPillar2Id(enrolments) match {
            case Some(pillar2Id) =>
              Future.successful(
                IdentifierRequest(
                  request = request,
                  userId = internalId,
                  groupId = Some(groupId),
                  clientPillar2Id = pillar2Id,
                  userIdForEnrolment = credentials.providerId
                )
              )
            case None =>
              logger.warn(s"Pillar2 ID not found in enrolments for user $internalId")
              Future.failed(AuthenticationError("Pillar2 ID not found in enrolments"))
          }
        case Some(_) ~ Some(_) ~ _ ~ Some(Agent) ~ Some(User) ~ Some(_) =>
          agentAuth[A](request, request.headers.get("X-Pillar2-Id"))
        case _ =>
          logger.warn("User failed authorization checks")
          Future.failed(AuthenticationError("Invalid credentials"))
      } recoverWith { case e: AuthorisationException =>
      logger.warn(s"Authorization failed: ${e.getMessage}")
      Future.failed(AuthenticationError("Not authorized"))
    }
  }

  def agentAuth[A](request: Request[A], pillar2Id: Option[String])(implicit hc: HeaderCarrier): Future[IdentifierRequest[A]] = {
    val retrievals = Retrievals.internalId and Retrievals.groupIdentifier and
      Retrievals.allEnrolments and Retrievals.affinityGroup and
      Retrievals.credentialRole and Retrievals.credentials
    pillar2Id match {
      case Some(pillar2IdValue) =>
        authorised(
          AuthProviders(GovernmentGateway) and
            AffinityGroup.Agent and
            Enrolment(HMRC_PILLAR2_ORG_KEY)
              .withIdentifier(ENROLMENT_IDENTIFIER, pillar2IdValue)
              .withDelegatedAuthRule(DELEGATED_AUTH_RULE)
        ).retrieve(retrievals) {
          case Some(internalId) ~ Some(groupId) ~ enrolments ~ Some(_) ~ _ ~ Some(credentials) =>
            logger.info(
              s"EnrolmentAuthIdentifierAction - Successfully retrieved Agent enrolment with enrolments=$enrolments -- credentials=$credentials"
            )
            Future.successful(
              IdentifierRequest(
                request,
                internalId,
                Some(groupId),
                clientPillar2Id = pillar2IdValue,
                userIdForEnrolment = credentials.providerId
              )
            )
          case _ =>
            Future.failed(AuthenticationError("Agent is unauthorised"))
        }

      case None =>
        Future.failed(
          AuthenticationError(
            "Please provide the request header for your client, to check it contains the Pillar 2 ID they were assigned at registration."
          )
        )
    }
  }
}

object AuthenticatedIdentifierAction {
  val HMRC_PILLAR2_ORG_KEY = "HMRC-PILLAR2-ORG"
  val ENROLMENT_IDENTIFIER = "PLRID"
  val DELEGATED_AUTH_RULE  = "pillar2-auth"
}
