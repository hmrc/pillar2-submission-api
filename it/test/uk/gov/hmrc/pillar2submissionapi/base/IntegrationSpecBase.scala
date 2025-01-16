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
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.{Application, inject}
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Retrieval, ~}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.HttpClientSupport
import uk.gov.hmrc.pillar2submissionapi.base.TestAuthRetrievals.Ops
import uk.gov.hmrc.pillar2submissionapi.controllers.actions.{AuthenticatedIdentifierAction, IdentifierAction}
import uk.gov.hmrc.pillar2submissionapi.helpers.{SubscriptionDataFixture, UKTaxReturnDataFixture, WireMockServerHandler}

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

trait IntegrationSpecBase
    extends AnyWordSpec
    with Matchers
    with Results
    with MockitoSugar
    with WireMockServerHandler
    with HttpClientSupport
    with GuiceOneServerPerSuite
    with SubscriptionDataFixture
    with UKTaxReturnDataFixture
    with BeforeAndAfterEach {

  implicit lazy val system:       ActorSystem      = ActorSystem()
  implicit lazy val materializer: Materializer     = Materializer(system)
  implicit lazy val ec:           ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit lazy val hc:           HeaderCarrier    = new HeaderCarrier

  type RetrievalsType = Option[String] ~ Option[String] ~ Enrolments ~ Option[AffinityGroup] ~ Option[CredentialRole] ~ Option[Credentials]

  val fakeRequest: Request[AnyContent] = FakeRequest(method = "", path = "")

  val plrReference         = "XCCVRUGFJG788"
  val readSubscriptionPath = "/report-pillar2-top-up-taxes/subscription/read-subscription"

  val HMRC_PILLAR2_ORG_KEY = "HMRC-PILLAR2-ORG"
  val ENROLMENT_IDENTIFIER = "PLRID"
  val DELEGATED_AUTH_RULE  = "pillar2-auth"

  val pillar2Enrolments: Enrolments = Enrolments(
    Set(Enrolment(HMRC_PILLAR2_ORG_KEY, Seq(EnrolmentIdentifier(ENROLMENT_IDENTIFIER, plrReference)), "", None))
  )

  val requiredGatewayPredicate: Predicate = AuthProviders(GovernmentGateway) and ConfidenceLevel.L50
  val requiredAgentPredicate: Predicate = AuthProviders(GovernmentGateway) and AffinityGroup.Agent and
    Enrolment(HMRC_PILLAR2_ORG_KEY)
      .withIdentifier(ENROLMENT_IDENTIFIER, plrReference)
      .withDelegatedAuthRule(DELEGATED_AUTH_RULE)
  val requiredRetrievals
    : Retrieval[Option[String] ~ Option[String] ~ Enrolments ~ Option[AffinityGroup] ~ Option[CredentialRole] ~ Option[Credentials]] =
    Retrievals.internalId and Retrievals.groupIdentifier and
      Retrievals.allEnrolments and Retrievals.affinityGroup and
      Retrievals.credentialRole and Retrievals.credentials

  val id:           String = UUID.randomUUID().toString
  val groupId:      String = UUID.randomUUID().toString
  val providerId:   String = UUID.randomUUID().toString
  val providerType: String = UUID.randomUUID().toString

  val mockAuthConnector: AuthConnector = mock[AuthConnector]

  override def beforeEach(): Unit = {
    when(
      mockAuthConnector.authorise[RetrievalsType](any[Predicate](), any[Retrieval[RetrievalsType]]())(any[HeaderCarrier](), any[ExecutionContext]())
    )
      .thenReturn(
        Future.successful(
          Some(id) ~ Some(groupId) ~ pillar2Enrolments ~ Some(Organisation) ~ Some(User) ~ Some(Credentials(providerId, providerType))
        )
      )
    super.beforeEach()
  }

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure("microservice.services.pillar2.port" -> wiremockPort)
    .overrides(
      inject.bind[IdentifierAction].toInstance(new AuthenticatedIdentifierAction(mockAuthConnector, new BodyParsers.Default()))
    )
    .build()

}
