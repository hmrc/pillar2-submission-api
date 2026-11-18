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
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Organisation}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames}
import uk.gov.hmrc.pillar2submissionapi.config.AppConfig
import uk.gov.hmrc.pillar2submissionapi.models.audit.{AuthDetails, AuthorisationFailureAuditEvent}
import uk.gov.hmrc.pillar2submissionapi.models.error.Pillar2Error
import uk.gov.hmrc.pillar2submissionapi.models.error.Pillar2Error.*
import uk.gov.hmrc.pillar2submissionapi.models.requests.IdentifierRequest
import uk.gov.hmrc.pillar2submissionapi.services.AuditService
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class AuthenticatedIdentifierAction @Inject() (
  override val authConnector: AuthConnector,
  val config:                 AppConfig,
  auditService:               AuditService
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

  private def authDetails(retrieved: AuthRetrievals): AuthDetails = retrieved match {
    case internalId ~ groupId ~ enrolments ~ affinityGroup ~ credentialRole ~ credentials =>
      AuthDetails(
        internalId = internalId,
        groupId = groupId,
        credId = credentials.map(_.providerId),
        affinityGroup = affinityGroup.map(_.toString),
        credentialRole = credentialRole.map(_.toString),
        enrolmentPillar2Id = getPillar2Id(enrolments),
        enrolments = enrolments.enrolments.toSeq.map(enrolment => s"${enrolment.key}:${enrolment.state}").sorted
      )
  }

  private def recordFailure(
    message: String,
    error:   Pillar2Error,
    request: RequestWithPillar2Id[?],
    user:    Option[AuthDetails] = None,
    reason:  Option[String] = None
  )(using hc: HeaderCarrier): Unit = {
    val userLog = user.fold("") { details =>
      s" userId=${details.internalId.getOrElse("-")} affinityGroup=${details.affinityGroup.getOrElse("-")}" +
        s" credentialRole=${details.credentialRole.getOrElse("-")} enrolments=[${details.enrolments.mkString(",")}]"
    }
    logger.warn(
      s"$message - code=${error.code} pillar2Id=${request.pillar2Id}" +
        reason.fold("")(r => s" reason=$r") + userLog
    )

    val event = AuthorisationFailureAuditEvent(request.pillar2Id, error.code, reason, user)
    Try(auditService.auditAuthorisationFailure(event)).failed.foreach(e => logger.error("Failed to send the audit event", e))
  }

  private def processPillar2Enrolment[A](
    request:    RequestWithPillar2Id[A],
    enrolments: Enrolments,
    internalId: String,
    groupId:    String,
    providerId: String,
    retrieved:  AuthRetrievals
  )(using hc: HeaderCarrier): Future[IdentifierRequest[A]] = getPillar2Id(
    enrolments
  ) match {
    case Some(pillar2Id) =>
      if request.pillar2Id != pillar2Id then {
        recordFailure("X-Pillar2-Id header does not match the Pillar2 enrolment", IncorrectHeaderValueError, request, Some(authDetails(retrieved)))
        throw IncorrectHeaderValueError
      } else
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
      recordFailure("Invalid Pillar2 enrolment", InvalidEnrolmentError, request, Some(authDetails(retrieved)))
      Future.failed(InvalidEnrolmentError)
  }

  override protected def transform[A](request: RequestWithPillar2Id[A]): Future[IdentifierRequest[A]] = {
    given hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    if !request.headers.get(HeaderNames.authorisation).exists(_.trim.nonEmpty) then {
      recordFailure("Authorization header is missing", MissingCredentialsError, request)
      throw MissingCredentialsError
    } else {
      val retrievals = Retrievals.internalId and Retrievals.groupIdentifier and
        Retrievals.allEnrolments and Retrievals.affinityGroup and
        Retrievals.credentialRole and Retrievals.credentials

      authorised(AuthProviders(GovernmentGateway) and ConfidenceLevel.L50)
        .retrieve(retrievals) { retrieved =>
          retrieved match {
            case Some(internalId) ~ Some(groupId) ~ enrolments ~ Some(Organisation) ~ None ~ Some(credentials) if config.allowTestUsers =>
              processPillar2Enrolment(request, enrolments, internalId, groupId, credentials.providerId, retrieved)
            case Some(internalId) ~ Some(groupId) ~ enrolments ~ Some(Organisation) ~ Some(User) ~ Some(credentials) =>
              processPillar2Enrolment(request, enrolments, internalId, groupId, credentials.providerId, retrieved)
            case Some(_) ~ Some(_) ~ _ ~ Some(Agent) ~ Some(User) ~ Some(_) =>
              agentAuth[A](request, request.pillar2Id)
            case Some(_) ~ Some(_) ~ _ ~ Some(Agent) ~ Some(Assistant) ~ Some(_) =>
              agentAuth[A](request, request.pillar2Id)
            case Some(_) ~ Some(_) ~ _ ~ Some(Agent) ~ None ~ Some(_) if config.allowTestUsers =>
              agentAuth[A](request, request.pillar2Id)
            case _ =>
              recordFailure("User is not valid for this API", ForbiddenError, request, Some(authDetails(retrieved)))
              Future.failed(ForbiddenError)
          }
        } recoverWith { case e: AuthorisationException =>
        recordFailure("Authorization failed", InvalidCredentialsError, request, reason = Some(e.reason))
        Future.failed(InvalidCredentialsError)
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
      case _ =>
        recordFailure("Agent is not valid for this API", ForbiddenError, request)
        Future.failed(ForbiddenError)
    }
  }
}

object AuthenticatedIdentifierAction {
  val HMRC_PILLAR2_ORG_KEY = "HMRC-PILLAR2-ORG"
  val ENROLMENT_IDENTIFIER = "PLRID"
  val DELEGATED_AUTH_RULE  = "pillar2-auth"

  private type AuthRetrievals =
    Option[String] ~ Option[String] ~ Enrolments ~ Option[AffinityGroup] ~ Option[CredentialRole] ~ Option[Credentials]
}
