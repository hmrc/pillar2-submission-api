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

package uk.gov.hmrc.pillar2submissionapi.base

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc._
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Retrieval, ~}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.base.TestAuthRetrievals.Ops
import uk.gov.hmrc.pillar2submissionapi.connectors.SubscriptionConnector
import uk.gov.hmrc.pillar2submissionapi.controllers.actions.IdentifierActionSpec.{enrolmentKey, identifierName, identifierValue}
import uk.gov.hmrc.pillar2submissionapi.controllers.actions.{AuthenticatedIdentifierAction, IdentifierAction}
import uk.gov.hmrc.pillar2submissionapi.helpers.SubscriptionDataFixture

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

trait IntegrationSpecBase
    extends AnyWordSpec
    with GuiceOneServerPerSuite
    with BeforeAndAfterEach
    with Matchers
    with Results
    with MockitoSugar
    with SubscriptionDataFixture {

  implicit lazy val system:       ActorSystem      = ActorSystem()
  implicit lazy val materializer: Materializer     = Materializer(system)
  implicit lazy val ec:           ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit lazy val hc:           HeaderCarrier    = new HeaderCarrier

  type RetrievalsType = Option[String] ~ Option[String] ~ Enrolments ~ Option[AffinityGroup] ~ Option[CredentialRole] ~ Option[Credentials]

  val fakeRequest: Request[AnyContent] = FakeRequest(method = "", path = "")

  val pillar2Enrolments: Enrolments = Enrolments(
    Set(Enrolment(enrolmentKey, Seq(EnrolmentIdentifier(identifierName, identifierValue)), "", None))
  )
  val id:           String = UUID.randomUUID().toString
  val groupId:      String = UUID.randomUUID().toString
  val providerId:   String = UUID.randomUUID().toString
  val providerType: String = UUID.randomUUID().toString

  val mockAuthConnector:         AuthConnector         = mock[AuthConnector]
  val mockSubscriptionConnector: SubscriptionConnector = mock[SubscriptionConnector]

  when(mockAuthConnector.authorise[RetrievalsType](any[Predicate](), any[Retrieval[RetrievalsType]]())(any[HeaderCarrier](), any[ExecutionContext]()))
    .thenReturn(
      Future.successful(Some(id) ~ Some(groupId) ~ pillar2Enrolments ~ Some(Organisation) ~ Some(User) ~ Some(Credentials(providerId, providerType)))
    )

  when(mockSubscriptionConnector.readSubscription(any[String]())(any[HeaderCarrier](), any[ExecutionContext]()))
    .thenReturn(
      Future.successful(Right(subscriptionData))
    )

  protected def applicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure()
      .overrides(
        inject.bind[IdentifierAction].toInstance(new AuthenticatedIdentifierAction(mockAuthConnector, new BodyParsers.Default())),
        inject.bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
      )
}
