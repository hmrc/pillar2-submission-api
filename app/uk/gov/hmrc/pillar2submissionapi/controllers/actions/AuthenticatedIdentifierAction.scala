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
import play.api.libs.json._
import play.api.mvc.Results.BadRequest
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.pillar2submissionapi.connectors.EnrolmentStoreProxyConnector
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
  val enrolmentAuth:             EnrolmentStoreProxyConnector,
  val parser:                    BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends IdentifierAction
    with AuthorisedFunctions
    with Logging {

  private val HMRC_PILLAR2_ORG_KEY = "HMRC-PILLAR2-ORG"
  private val ENROLMENT_IDENTIFIER = "PLRID"
  private val DELEGATED_AUTH_RULE  = "pillar2-auth"
  private val HMRC_AS_AGENT_KEY    = "HMRC-AS-AGENT"

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
          val validated = for {
            pillar2Id <- getPillar2Id(enrolments).toRight(AuthenticationError(s"Pillar2 ID not found in enrolments"))
          } yield IdentifierRequest(
            request = request,
            userId = internalId,
            groupId = Some(groupId),
            clientPillar2Id = pillar2Id,
            userIdForEnrolment = credentials.providerId
          )
          validated.fold(Future.failed, Future.successful)
        case Some(internalId) ~ Some(groupId) ~ enrolments ~ Some(Agent) ~ Some(User) ~ Some(credentials) =>
          println(enrolments)
          def validateAgentEnrolment =
            Either.cond(
              enrolments.getEnrolment(HMRC_AS_AGENT_KEY).isDefined,
              (),
              AuthenticationError("Agent enrolment not found")
            )

          def validateDelegatedAuthority = {
            println(enrolments.getEnrolment(HMRC_PILLAR2_ORG_KEY).foreach { enrolment =>
              val enrolmentJson = Json.toJson(enrolment)
              println(s"Enrolment model as JSON: ${Json.prettyPrint(enrolmentJson)}")
            })
            println(enrolments.getEnrolment(HMRC_AS_AGENT_KEY).foreach { enrolment =>
              val enrolmentJson = Json.toJson(enrolment)
              println(s"Enrolment model as JSON: ${Json.prettyPrint(enrolmentJson)}")
            })
            println(groupId)
            println(enrolmentAuth.getDelegatedEnrolment(groupId))
            println(
              request.headers
                .get("pillar2-id")
                .toRight(AuthenticationError("Missing Pillar 2 ID"))
            )

            Either.cond(
              enrolments.getEnrolment(HMRC_PILLAR2_ORG_KEY).exists(_.delegatedAuthRule.contains(DELEGATED_AUTH_RULE)),
              (),
              AuthenticationError("Missing delegated authority")
            )
          }

          def validatePillar2Id =
            getPillar2Id(enrolments).toRight(AuthenticationError("Delegated Pillar2 ID not found in agent enrolments"))

          val validationResult = for {
            _         <- validateAgentEnrolment
            _         <- validateDelegatedAuthority
            pillar2Id <- validatePillar2Id
          } yield IdentifierRequest(
            request = request,
            userId = internalId,
            groupId = Some(groupId),
            clientPillar2Id = pillar2Id,
            userIdForEnrolment = credentials.providerId
          )

          validationResult.fold(Future.failed, Future.successful)
        case _ =>
          logger.warn("User failed authorization checks")
          Future.failed(AuthenticationError("Invalid credentials"))
      } recoverWith { case e: AuthorisationException =>
      logger.warn(s"Authorization failed: ${e.getMessage}")
      Future.failed(AuthenticationError("Not authorized"))
    }
  }
}
