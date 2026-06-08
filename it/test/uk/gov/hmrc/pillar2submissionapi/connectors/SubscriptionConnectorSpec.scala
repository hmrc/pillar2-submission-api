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

import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, OK}
import uk.gov.hmrc.pillar2submissionapi.base.IntegrationSpecBase
import uk.gov.hmrc.pillar2submissionapi.helpers.SubscriptionDataFixture

class SubscriptionConnectorSpec extends IntegrationSpecBase with SubscriptionDataFixture with EitherValues {

  private val unsuccessfulResponseJson = """{ "status": "error" }"""

  "readSubscription" when {
    "readSubscriptionV2Enabled is false" must {
      "return SubscriptionData (V1) when the backend responds 200 with valid JSON" in {
        val app: Application = guiceAppBuilder("features.readSubscriptionV2Enabled" -> false).build()
        val subscriptionConnector = app.injector.instanceOf[SubscriptionConnector]

        stubGet(s"$readSubscriptionPath/$plrReference", OK, subscriptionSuccessJson.toString)

        val result = subscriptionConnector.readSubscription(plrReference).futureValue

        result.isRight mustBe true
        result.value mustBe subscriptionData
      }

      "return a BadRequest when the backend responds 200 but with invalid JSON" in {
        val app: Application = guiceAppBuilder("features.readSubscriptionV2Enabled" -> false).build()
        val subscriptionConnector = app.injector.instanceOf[SubscriptionConnector]

        stubGet(s"$readSubscriptionPath/$plrReference", BAD_REQUEST, unsuccessfulResponseJson)

        val result = subscriptionConnector.readSubscription(plrReference).futureValue

        result.isLeft mustBe true
        result mustBe Left(BadRequest)
      }

      "return a BadRequest when the backend responses with a non-200 status" in {
        val app: Application = guiceAppBuilder("features.readSubscriptionV2Enabled" -> false).build()
        val subscriptionConnector = app.injector.instanceOf[SubscriptionConnector]

        stubGet(s"$readSubscriptionPath/$plrReference", BAD_REQUEST, unsuccessfulResponseJson)

        val result = subscriptionConnector.readSubscription(plrReference).futureValue

        result.isLeft mustBe true
        result mustBe Left(BadRequest)
      }
    }

    "readSubscriptionV2Enabled is true" must {
      "return SubscriptionData (V2) when the backend responds 200 with valid JSON" in {
        val app: Application = guiceAppBuilder("features.readSubscriptionV2Enabled" -> true).build()
        val subscriptionConnector = app.injector.instanceOf[SubscriptionConnector]

        stubGet(s"$readSubscriptionV2Path/$plrReference", OK, subscriptionSuccessV2Json.toString)

        val result = subscriptionConnector.readSubscription(plrReference).futureValue

        result.isRight mustBe true
        result.value mustBe subscriptionDataV2
      }

      "return a BadRequest when the backend responds 200 but with invalid JSON" in {
        val app: Application = guiceAppBuilder("features.readSubscriptionV2Enabled" -> true).build()
        val subscriptionConnector = app.injector.instanceOf[SubscriptionConnector]

        stubGet(s"$readSubscriptionV2Path/$plrReference", BAD_REQUEST, unsuccessfulResponseJson)

        val result = subscriptionConnector.readSubscription(plrReference).futureValue

        result.isLeft mustBe true
        result mustBe Left(BadRequest)
      }

      "return a BadRequest when the backend responses with a non-200 status" in {
        val app: Application = guiceAppBuilder("features.readSubscriptionV2Enabled" -> true).build()
        val subscriptionConnector = app.injector.instanceOf[SubscriptionConnector]

        stubGet(s"$readSubscriptionV2Path/$plrReference", BAD_REQUEST, unsuccessfulResponseJson)

        val result = subscriptionConnector.readSubscription(plrReference).futureValue

        result.isLeft mustBe true
        result mustBe Left(BadRequest)
      }
    }
  }
}
