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
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.{Application, inject}
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Retrieval, ~}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.HttpClientSupport
import uk.gov.hmrc.pillar2submissionapi.WireMockServerHandler
import uk.gov.hmrc.pillar2submissionapi.base.TestAuthRetrievals.Ops
import uk.gov.hmrc.pillar2submissionapi.controllers.actions.AuthenticatedIdentifierActionSpec.{enrolmentKey, identifierName, identifierValue}
import uk.gov.hmrc.pillar2submissionapi.controllers.actions.{AuthenticatedIdentifierAction, IdentifierAction}
import uk.gov.hmrc.pillar2submissionapi.helpers.SubscriptionDataFixture
import uk.gov.hmrc.pillar2submissionapi.models.subscription.{SubscriptionData, SubscriptionSuccess}

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

trait IntegrationSpecBase
    extends AnyFlatSpec
    with Matchers
    with Results
    with MockitoSugar
    with WireMockServerHandler
    with HttpClientSupport
    with GuiceOneServerPerSuite
    with SubscriptionDataFixture
    with BeforeAndAfterEach {

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

  val plrReference         = "XCCVRUGFJG788"
  val readSubscriptionPath = "/report-pillar2-top-up-taxes/subscription/read-subscription"

  val successfulResponseJson =
    """
      |{
      |
      |      "formBundleNumber": "119000004320",
      |      "upeDetails": {
      |          "domesticOnly": false,
      |          "organisationName": "International Organisation Inc.",
      |          "customerIdentification1": "12345678",
      |          "customerIdentification2": "12345678",
      |          "registrationDate": "2022-01-31",
      |          "filingMember": false
      |      },
      |      "upeCorrespAddressDetails": {
      |          "addressLine1": "1 High Street",
      |          "addressLine2": "Egham",
      |
      |          "addressLine3": "Wycombe",
      |          "addressLine4": "Surrey",
      |          "postCode": "HP13 6TT",
      |          "countryCode": "GB"
      |      },
      |      "primaryContactDetails": {
      |          "name": "Fred Flintstone",
      |          "telephone": "0115 9700 700",
      |          "emailAddress": "fred.flintstone@aol.com"
      |      },
      |      "secondaryContactDetails": {
      |          "name": "Donald Trump",
      |          "telephone": "0115 9700 701",
      |          "emailAddress": "donald.trump@potus.com"
      |
      |      },
      |      "filingMemberDetails": {
      |          "safeId": "XL6967739016188",
      |          "organisationName": "Domestic Operations Ltd",
      |          "customerIdentification1": "1234Z678",
      |          "customerIdentification2": "1234567Y"
      |      },
      |      "accountingPeriod": {
      |          "startDate": "2024-01-06",
      |          "endDate": "2025-04-06",
      |          "duetDate": "2024-04-06"
      |      },
      |      "accountStatus": {
      |          "inactive": true
      |      }
      |  }
      |""".stripMargin

  val subscriptionDataJson = Json.parse(successfulResponseJson).as[SubscriptionData]

  val subscriptionSuccess: JsValue = Json.toJson(SubscriptionSuccess(subscriptionDataJson))

}
