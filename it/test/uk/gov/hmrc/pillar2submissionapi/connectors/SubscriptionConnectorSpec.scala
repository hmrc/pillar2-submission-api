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

package uk.gov.hmrc.pillar2submissionapi.connectors

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Gen
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.Application
import play.api.http.Status.OK
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.base.IntegrationSpecBase
import uk.gov.hmrc.pillar2submissionapi.connectors.SubscriptionConnectorSpec._
import uk.gov.hmrc.pillar2submissionapi.helpers._
import uk.gov.hmrc.pillar2submissionapi.models.subscription.{SubscriptionData, SubscriptionSuccess}

import scala.concurrent.{ExecutionContext, Future}

class SubscriptionConnectorSpec extends IntegrationSpecBase with SubscriptionDataFixture {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.pillar2.port" -> server.port()
    )
    .build()

  private val subscriptionDataJson = Json.parse(successfulResponseJson).as[SubscriptionData]
  val subscriptionSuccess: JsValue = Json.toJson(SubscriptionSuccess(subscriptionDataJson))

  "SubscriptionConnector" must {
    "readSubscription" must {

      "return json when the backend has returned 200 OK with data" in {
        when(mockSubscriptionConnector.readSubscription(any[String]())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(
            Future.successful(Right(subscriptionDataJson))
          )

        stubGet(s"$readSubscriptionPath/$plrReference", OK, subscriptionSuccess.toString)
        val result: Either[Result, SubscriptionData] = mockSubscriptionConnector.readSubscription(plrReference).futureValue
        result.isRight mustBe true
        result mustBe Right(subscriptionDataJson)
      }

      "return a BadRequest when the backend has returned a response else than 200 status" in {
        when(mockSubscriptionConnector.readSubscription(any[String]())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(
            Future.successful(Left(BadRequest))
          )

        stubGet(s"$readSubscriptionPath/$plrReference", errorCodes.sample.value, unsuccessfulResponseJson)
        val result = mockSubscriptionConnector.readSubscription(plrReference).futureValue
        result.isLeft mustBe true
        result mustBe Left(BadRequest)
      }
    }
  }
}

object SubscriptionConnectorSpec {
  private val errorCodes: Gen[Int] = Gen.oneOf(Seq(400, 403, 500, 501, 502, 503, 504))
  private val plrReference         = "testPlrRef"
  private val readSubscriptionPath = "/report-pillar2-top-up-taxes/subscription/read-subscription"

  private val successfulResponseJson =
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

  private val unsuccessfulResponseJson = """{ "status": "error" }"""
}
