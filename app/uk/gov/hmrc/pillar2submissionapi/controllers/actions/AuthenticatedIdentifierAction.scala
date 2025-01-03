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
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}
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

  private val HMRC_PILLAR2_ORG_KEY = "HMRC-PILLAR2-ORG"
  private val ENROLMENT_IDENTIFIER = "PLRID"
  private val DELEGATED_AUTH_RULE  = "pillar2-auth"

  private def getPillar2Id(enrolments: Enrolments): Option[String] =
    for {
      pillar2Enrolment <- enrolments.getEnrolment(HMRC_PILLAR2_ORG_KEY)
      identifier       <- pillar2Enrolment.getIdentifier(ENROLMENT_IDENTIFIER)
    } yield identifier.value

  override protected def transform[A](request: Request[A]): Future[IdentifierRequest[A]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    val retrievals = Retrievals.internalId and
      Retrievals.allEnrolments and Retrievals.affinityGroup and
      Retrievals.credentialRole and Retrievals.credentials

    def orgAuth(internalId: String, enrolments: Enrolments, credentials: Credentials): Future[IdentifierRequest[A]] = {
      val validated = for {
        pillar2Id <- getPillar2Id(enrolments).toRight(AuthenticationError(s"Pillar2 ID not found in enrolments"))
      } yield IdentifierRequest(
        request = request,
        userId = internalId,
        clientPillar2Id = pillar2Id,
        userIdForEnrolment = credentials.providerId
      )
      validated.fold(Future.failed, Future.successful)
    }

    def agentAuth(internalId: String, enrolments: Enrolments, credentials: Credentials): Future[IdentifierRequest[A]] =
      request.headers.get("x-pillar2-id") match {
        case None =>
          Future.failed(AuthenticationError("Agent must provide a x-pillar2-id header"))

        case Some(pillar2IdValue) =>
          authorised(
            AuthProviders(GovernmentGateway) and Enrolment(HMRC_PILLAR2_ORG_KEY)
              .withIdentifier(ENROLMENT_IDENTIFIER, pillar2IdValue)
              .withDelegatedAuthRule(DELEGATED_AUTH_RULE)
          ) {
            logger.info(
              s"EnrolmentAuthIdentifierAction - Successfully retrieved Agent enrolment with enrolments=$enrolments -- credentials=$credentials"
            )
            Future.successful(
              IdentifierRequest(
                request,
                internalId,
                clientPillar2Id = pillar2IdValue,
                userIdForEnrolment = credentials.providerId
              )
            )
          }
      }

    authorised(AuthProviders(GovernmentGateway))
      .retrieve(retrievals) {
        case Some(internalId) ~ enrolments ~ Some(Organisation) ~ Some(User) ~ Some(credentials) =>
          orgAuth(internalId, enrolments, credentials)
        case Some(internalId) ~ enrolments ~ Some(Agent) ~ Some(User) ~ Some(credentials) =>
          agentAuth(internalId, enrolments, credentials)
        case _ =>
          logger.warn("User failed authorization checks")
          Future.failed(AuthenticationError("Invalid credentials"))
      }
  }
}
