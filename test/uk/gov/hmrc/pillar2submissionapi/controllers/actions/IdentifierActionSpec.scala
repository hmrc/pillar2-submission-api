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
import uk.gov.hmrc.pillar2submissionapi.controllers.actions.IdentifierActionSpec._
import uk.gov.hmrc.pillar2submissionapi.controllers.actions.base.ActionBaseSpec
import uk.gov.hmrc.pillar2submissionapi.controllers.base.TestAuthRetrievals.Ops

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class IdentifierActionSpec extends ActionBaseSpec {

  val identifierAction: AuthenticatedIdentifierAction = new AuthenticatedIdentifierAction(
    mockAuthConnector,
    new BodyParsers.Default
  )(ec)

  "IdentifierAction - different types of user" when {
    "a user is a registered organisation" must {
      "user is successfully authorized" in {
        when(
          mockAuthConnector.authorise[RetrievalsType](ArgumentMatchers.eq(requiredPredicate), ArgumentMatchers.eq(requiredRetrievals))(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(
              Some(id) ~ Some(groupId) ~ Some(clientId) ~ Some(Organisation) ~ Some(User) ~ Some(Credentials(providerId, providerType))
            )
          )

        val result = await(identifierAction.refine(fakeRequest))

        result.isRight.must(be(true))
        result.map { identifierRequest =>
          identifierRequest.userId             must be(id)
          identifierRequest.groupId            must be(Some(groupId))
          identifierRequest.clientPillar2Id    must be(clientId)
          identifierRequest.userIdForEnrolment must be(providerId)
        }
      }
    }

    "a user is a registered Agent" should {
      "user is successfully authorized" in {
        when(
          mockAuthConnector.authorise[RetrievalsType](ArgumentMatchers.eq(requiredPredicate), ArgumentMatchers.eq(requiredRetrievals))(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(Some(id) ~ Some(groupId) ~ Some(clientId) ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType)))
          )

        val result = await(identifierAction.refine(fakeRequest))

        result.isRight must be(true)
        result.map { identifierRequest =>
          identifierRequest.userId             must be(id)
          identifierRequest.groupId            must be(Some(groupId))
          identifierRequest.clientPillar2Id    must be(clientId)
          identifierRequest.userIdForEnrolment must be(providerId)
        }
      }
    }

    "a user is a registered Individual" should {
      "user is unauthorized" in {
        when(
          mockAuthConnector.authorise[RetrievalsType](ArgumentMatchers.eq(requiredPredicate), ArgumentMatchers.eq(requiredRetrievals))(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(Some(id) ~ Some(groupId) ~ Some(clientId) ~ Some(Individual) ~ Some(User) ~ Some(Credentials(providerId, providerType)))
          )

        val result = await(
          identifierAction.refine(fakeRequest)
        )

        result.isRight            must be(false)
        result.left.getOrElse("") must be(Unauthorized)
      }
    }
  }

  "IdentifierAction - missing data from auth response" when {
    "internalId missing" should {
      "user is unauthorized" in {
        when(
          mockAuthConnector.authorise[RetrievalsType](ArgumentMatchers.eq(requiredPredicate), ArgumentMatchers.eq(requiredRetrievals))(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(None ~ Some(groupId) ~ Some(clientId) ~ Some(Organisation) ~ Some(User) ~ Some(Credentials(providerId, providerType)))
          )

        val result = await(
          identifierAction.refine(fakeRequest)
        )

        result.isRight            must be(false)
        result.left.getOrElse("") must be(Unauthorized)
      }
    }

    "groupId missing" should {
      "user is unauthorized" in {
        when(
          mockAuthConnector.authorise[RetrievalsType](ArgumentMatchers.eq(requiredPredicate), ArgumentMatchers.eq(requiredRetrievals))(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(Some(id) ~ None ~ Some(clientId) ~ Some(Organisation) ~ Some(User) ~ Some(Credentials(providerId, providerType)))
          )

        val result = await(
          identifierAction.refine(fakeRequest)
        )

        result.isRight            must be(false)
        result.left.getOrElse("") must be(Unauthorized)
      }
    }

    "affinityGroup missing" should {
      "user is unauthorized" in {
        when(
          mockAuthConnector.authorise[RetrievalsType](ArgumentMatchers.eq(requiredPredicate), ArgumentMatchers.eq(requiredRetrievals))(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(Some(id) ~ Some(groupId) ~ Some(clientId) ~ None ~ Some(User) ~ Some(Credentials(providerId, providerType)))
          )

        val result = await(
          identifierAction.refine(fakeRequest)
        )

        result.isRight            must be(false)
        result.left.getOrElse("") must be(Unauthorized)
      }
    }

    "user missing" should {
      "user is unauthorized" in {
        when(
          mockAuthConnector.authorise[RetrievalsType](ArgumentMatchers.eq(requiredPredicate), ArgumentMatchers.eq(requiredRetrievals))(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(Some(id) ~ Some(groupId) ~ Some(clientId) ~ Some(Organisation) ~ None ~ Some(Credentials(providerId, providerType)))
          )

        val result = await(
          identifierAction.refine(fakeRequest)
        )

        result.isRight            must be(false)
        result.left.getOrElse("") must be(Unauthorized)
      }
    }
  }
}

object IdentifierActionSpec {
  type RetrievalsType = Option[String] ~ Option[String] ~ Option[String] ~ Option[AffinityGroup] ~ Option[CredentialRole] ~ Option[Credentials]

  val fakeRequest: Request[AnyContent] = FakeRequest(method = "", path = "")

  val requiredPredicate: Predicate = AuthProviders(GovernmentGateway) and ConfidenceLevel.L50
  val requiredRetrievals
    : Retrieval[Option[String] ~ Option[String] ~ Option[String] ~ Option[AffinityGroup] ~ Option[CredentialRole] ~ Option[Credentials]] =
    Retrievals.internalId and Retrievals.groupIdentifier and
      Retrievals.clientId and Retrievals.affinityGroup and
      Retrievals.credentialRole and Retrievals.credentials

  val clientId:     String = UUID.randomUUID().toString
  val id:           String = UUID.randomUUID().toString
  val groupId:      String = UUID.randomUUID().toString
  val providerId:   String = UUID.randomUUID().toString
  val providerType: String = UUID.randomUUID().toString

}