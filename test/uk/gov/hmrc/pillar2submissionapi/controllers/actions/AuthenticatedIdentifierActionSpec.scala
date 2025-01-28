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

import com.google.inject.Inject
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Retrieval, ~}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.base.ActionBaseSpec
import uk.gov.hmrc.pillar2submissionapi.controllers.actions.AuthenticatedIdentifierActionSpec._
import uk.gov.hmrc.pillar2submissionapi.controllers.error.AuthenticationError
import uk.gov.hmrc.pillar2submissionapi.controllers.error.ForbiddenError
import uk.gov.hmrc.pillar2submissionapi.controllers.error.MissingHeader
import uk.gov.hmrc.pillar2submissionapi.helpers.TestAuthRetrievals._

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

import AuthenticatedIdentifierAction._

class AuthenticatedIdentifierActionSpec extends ActionBaseSpec {

  val identifierAction: AuthenticatedIdentifierAction = new AuthenticatedIdentifierAction(
    mockAuthConnector,
    new BodyParsers.Default
  )(ec)

  "IdentifierAction - different types of user" when {
    "a user is a registered organisation" must {
      "user is successfully authorized" in {
        when(
          mockAuthConnector.authorise[RetrievalsType](ArgumentMatchers.eq(requiredOrgPredicate), ArgumentMatchers.eq(requiredRetrievals))(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(
              Some(id) ~ Some(groupId) ~ pillar2Enrolments ~ Some(Organisation) ~ Some(User) ~ Some(Credentials(providerId, providerType))
            )
          )

        val result = await(identifierAction.refine(fakeRequest))

        result.map { identifierRequest =>
          identifierRequest.userId             must be(id)
          identifierRequest.groupId            must be(Some(groupId))
          identifierRequest.clientPillar2Id    must be(identifierValue)
          identifierRequest.userIdForEnrolment must be(providerId)
        }
      }
    }

    "a user is a registered Agent" should {
      "pass if agent is a delegated entity of an org" in {

        when(
          mockAuthConnector.authorise[RetrievalsType](ArgumentMatchers.eq(requiredOrgPredicate), ArgumentMatchers.eq(requiredRetrievals))(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(Some(id) ~ Some(groupId) ~ pillar2Enrolments ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType)))
          )

        when(
          mockAuthConnector.authorise[RetrievalsType](ArgumentMatchers.eq(requiredAgentPredicate), ArgumentMatchers.eq(requiredRetrievals))(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(Some(id) ~ Some(groupId) ~ pillar2Enrolments ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType)))
          )

        val fakeRequest: Request[AnyContent] = FakeRequest().withHeaders("X-Pillar2-Id" -> identifierValue)
        val result = await(identifierAction.refine(fakeRequest))

        result.map { identifierRequest =>
          identifierRequest.userId             must be(id)
          identifierRequest.groupId            must be(Some(groupId))
          identifierRequest.clientPillar2Id    must be(identifierValue)
          identifierRequest.userIdForEnrolment must be(providerId)
        }
      }

      "fail due to missing X-Pillar2-Id header" in {
        when(
          mockAuthConnector.authorise[RetrievalsType](ArgumentMatchers.eq(requiredOrgPredicate), ArgumentMatchers.eq(requiredRetrievals))(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(Some(id) ~ Some(groupId) ~ pillar2Enrolments ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType)))
          )

        val result = intercept[MissingHeader](await(identifierAction.refine(fakeRequest)))

        result.message mustEqual "Please provide the X-Pillar2-Id header containing the Pillar 2 ID your client was assigned at registration."
      }

      "fail due to user being unauthorised" in {

        when(
          mockAuthConnector.authorise[RetrievalsType](ArgumentMatchers.eq(requiredOrgPredicate), ArgumentMatchers.eq(requiredRetrievals))(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(Some(id) ~ Some(groupId) ~ pillar2Enrolments ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType)))
          )

        when(
          mockAuthConnector.authorise[RetrievalsType](ArgumentMatchers.eq(requiredAgentPredicate), ArgumentMatchers.eq(requiredRetrievals))(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenThrow(FailedRelationship("NO_RELATIONSHIP;HMRC-PILLAR2-ORG"))

        val fakeRequest: Request[AnyContent] = FakeRequest().withHeaders("X-Pillar2-Id" -> identifierValue)
        val result = intercept[AuthenticationError.type](await(identifierAction.refine(fakeRequest)))

        result.message mustEqual "Not authorized"
      }
    }

    "a user is a registered Individual" should {
      "fail as user is unauthorized" in {
        when(
          mockAuthConnector.authorise[RetrievalsType](ArgumentMatchers.eq(requiredOrgPredicate), ArgumentMatchers.eq(requiredRetrievals))(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(
              Some(id) ~ Some(groupId) ~ pillar2Enrolments ~ Some(Individual) ~ Some(User) ~ Some(Credentials(providerId, providerType))
            )
          )

        val result = intercept[ForbiddenError.type](
          await(
            identifierAction.refine(fakeRequest)
          )
        )

        result.message mustEqual "Forbidden"
      }
    }
  }

  "IdentifierAction - missing data from auth response" when {
    "internalId missing" should {
      "user is unauthorized" in {
        when(
          mockAuthConnector.authorise[RetrievalsType](ArgumentMatchers.eq(requiredOrgPredicate), ArgumentMatchers.eq(requiredRetrievals))(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(
              None ~ Some(groupId) ~ pillar2Enrolments ~ Some(Organisation) ~ Some(User) ~ Some(Credentials(providerId, providerType))
            )
          )

        val result = intercept[ForbiddenError.type](
          await(
            identifierAction.refine(fakeRequest)
          )
        )

        result.message mustEqual "Forbidden"
      }
    }

    "groupId missing" should {
      "user is unauthorized" in {
        when(
          mockAuthConnector.authorise[RetrievalsType](ArgumentMatchers.eq(requiredOrgPredicate), ArgumentMatchers.eq(requiredRetrievals))(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(Some(id) ~ None ~ pillar2Enrolments ~ Some(Organisation) ~ Some(User) ~ Some(Credentials(providerId, providerType)))
          )

        val result = intercept[ForbiddenError.type](
          await(
            identifierAction.refine(fakeRequest)
          )
        )

        result.message mustEqual "Forbidden"
      }
    }

    "affinityGroup missing" should {
      "user is unauthorized" in {
        when(
          mockAuthConnector.authorise[RetrievalsType](ArgumentMatchers.eq(requiredOrgPredicate), ArgumentMatchers.eq(requiredRetrievals))(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(Some(id) ~ Some(groupId) ~ pillar2Enrolments ~ None ~ Some(User) ~ Some(Credentials(providerId, providerType)))
          )

        val result = intercept[ForbiddenError.type](
          await(
            identifierAction.refine(fakeRequest)
          )
        )

        result.message mustEqual "Forbidden"
      }
    }

    "user missing" should {
      "user is unauthorized" in {
        when(
          mockAuthConnector.authorise[RetrievalsType](ArgumentMatchers.eq(requiredOrgPredicate), ArgumentMatchers.eq(requiredRetrievals))(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(Some(id) ~ Some(groupId) ~ pillar2Enrolments ~ Some(Organisation) ~ None ~ Some(Credentials(providerId, providerType)))
          )

        val result = intercept[ForbiddenError.type](
          await(
            identifierAction.refine(fakeRequest)
          )
        )

        result.message mustEqual "Forbidden"
      }
    }
  }

  "IdentifierAction - invalid details" when {
    "pillar2Id is missing" should {
      "user is unauthorized" in {
        when(
          mockAuthConnector.authorise[RetrievalsType](ArgumentMatchers.eq(requiredOrgPredicate), ArgumentMatchers.eq(requiredRetrievals))(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(
              Some(id) ~ Some(groupId) ~ Enrolments(Set.empty) ~ Some(Organisation) ~ Some(User) ~ Some(Credentials(providerId, providerType))
            )
          )

        val result = intercept[ForbiddenError.type](
          await(
            identifierAction.refine(fakeRequest)
          )
        )

        result.message mustEqual "Forbidden"
      }
    }
  }

  "IdentifierAction - exceptions" when {
    "AuthorisationException is thrown" should {
      "user is unauthorized" in {
        val identifierAction: AuthenticatedIdentifierAction = new AuthenticatedIdentifierAction(
          new FakeFailingAuthConnector,
          new BodyParsers.Default
        )(ec)

        val result = intercept[AuthenticationError.type](
          await(
            identifierAction.refine(fakeRequest)
          )
        )

        result.message mustEqual "Not authorized"
      }
    }
  }

  class FakeFailingAuthConnector @Inject() extends AuthConnector {
    override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
      Future.failed(new MissingBearerToken)
  }

}

object AuthenticatedIdentifierActionSpec {
  type RetrievalsType = Option[String] ~ Option[String] ~ Enrolments ~ Option[AffinityGroup] ~ Option[CredentialRole] ~ Option[Credentials]

  val fakeRequest: Request[AnyContent] = FakeRequest(method = "", path = "")
  val enrolmentKey    = "HMRC-PILLAR2-ORG"
  val identifierName  = "PLRID"
  val identifierValue = "XCCVRUGFJG788"

  val requiredOrgPredicate: Predicate = AuthProviders(GovernmentGateway) and ConfidenceLevel.L50
  val requiredAgentPredicate: Predicate = AuthProviders(GovernmentGateway) and AffinityGroup.Agent and
    Enrolment(HMRC_PILLAR2_ORG_KEY)
      .withIdentifier(ENROLMENT_IDENTIFIER, identifierValue)
      .withDelegatedAuthRule(DELEGATED_AUTH_RULE)
  val requiredRetrievals
    : Retrieval[Option[String] ~ Option[String] ~ Enrolments ~ Option[AffinityGroup] ~ Option[CredentialRole] ~ Option[Credentials]] =
    Retrievals.internalId and Retrievals.groupIdentifier and
      Retrievals.allEnrolments and Retrievals.affinityGroup and
      Retrievals.credentialRole and Retrievals.credentials

  val pillar2Enrolments: Enrolments = Enrolments(
    Set(Enrolment(enrolmentKey, Seq(EnrolmentIdentifier(identifierName, identifierValue)), "Activated", None))
  )
  val id:           String = UUID.randomUUID().toString
  val groupId:      String = UUID.randomUUID().toString
  val providerId:   String = UUID.randomUUID().toString
  val providerType: String = UUID.randomUUID().toString

}
