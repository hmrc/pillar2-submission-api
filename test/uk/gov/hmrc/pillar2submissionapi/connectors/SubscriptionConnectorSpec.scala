/*
 * Copyright 2026 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, getRequestedFor, urlEqualTo}
import org.scalatest.matchers.should.Matchers.should
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsObject
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import play.api.{Application, Configuration}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.base.UnitTestBaseSpec
import uk.gov.hmrc.pillar2submissionapi.helpers.SubscriptionDataFixture

class SubscriptionConnectorSpec extends UnitTestBaseSpec with SubscriptionDataFixture {

  lazy val v1App: Application = new GuiceApplicationBuilder()
    .configure(Configuration("microservice.services.pillar2.port" -> server.port()))
    .configure("features.readSubscriptionV2Enabled" -> false)
    .build()

  lazy val v2App: Application = new GuiceApplicationBuilder()
    .configure(Configuration("microservice.services.pillar2.port" -> server.port()))
    .configure("features.readSubscriptionV2Enabled" -> true)
    .build()

  lazy val subscriptionConnectorV1: SubscriptionConnector = v1App.injector.instanceOf[SubscriptionConnector]
  lazy val subscriptionConnectorV2: SubscriptionConnector = v2App.injector.instanceOf[SubscriptionConnector]

  private val plrReference = "XAPLR0000000001"

  private val readSubscriptionUrl   = s"$readSubscriptionPath/$plrReference"
  private val readSubscriptionV2Url = s"$readSubscriptionV2Path/$plrReference"

  private val invalidSubscriptionJson: JsObject = JsObject.empty

  "SubscriptionConnector.readSubscription" when {

    "readSubscriptionV2Enabled is false" must {
      "verify the test application configuration has the V2 feature disabled" in {
        v1App.configuration.get[Boolean]("features.readSubscriptionV2Enabled") mustBe false
      }

      "forward the X-Pillar2-Id header" in {
        given hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("X-Pillar2-Id" -> pillar2Id)
        stubRequestWithPillar2Id("GET", readSubscriptionUrl, OK, subscriptionSuccessJson)

        val result = await(subscriptionConnectorV1.readSubscription(plrReference))

        result.isRight mustBe true
        server.verify(
          getRequestedFor(urlEqualTo(readSubscriptionUrl)).withHeader("X-Pillar2-Id", equalTo(pillar2Id))
        )
      }

      "return json when the backend has returned 200 OK with data" in {
        given hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("X-Pillar2-Id" -> pillar2Id)
        stubRequestWithPillar2Id("GET", readSubscriptionUrl, OK, subscriptionSuccessJson)

        val result = await(subscriptionConnectorV1.readSubscription(plrReference))

        result.isRight mustBe true
        result mustBe Right(subscriptionData)

        server.verify(
          getRequestedFor(urlEqualTo(readSubscriptionUrl)).withHeader("X-Pillar2-Id", equalTo(pillar2Id))
        )
      }

      "return BadRequest when ETMP returns 200 with an unparseable body" in {
        given hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("X-Pillar2-Id" -> pillar2Id)
        stubRequestWithPillar2Id("GET", readSubscriptionV2Url, OK, invalidSubscriptionJson)

        await(subscriptionConnectorV2.readSubscription(plrReference)) mustBe Left(BadRequest)
      }

      "return BadRequest when ETMP returns non-200" in {
        given hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("X-Pillar2-Id" -> pillar2Id)
        stubRequestWithPillar2Id("GET", readSubscriptionUrl, BAD_REQUEST, invalidSubscriptionJson)

        val result = await(subscriptionConnectorV1.readSubscription(plrReference))

        result.isLeft mustBe true
        result mustBe Left(BadRequest)
      }
    }

    "readSubscriptionV2Enabled is true" must {

      "verify the test application configuration has the V2 feature enabled" in {
        v2App.configuration.get[Boolean]("features.readSubscriptionV2Enabled") mustBe true
      }

      "forward the X-Pillar2-Id header" in {
        given hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("X-Pillar2-Id" -> pillar2Id)
        stubRequestWithPillar2Id("GET", readSubscriptionV2Url, OK, subscriptionSuccessV2Json)

        val result = await(subscriptionConnectorV2.readSubscription(plrReference))

        result.isRight mustBe true
        server.verify(
          getRequestedFor(urlEqualTo(readSubscriptionV2Url)).withHeader("X-Pillar2-Id", equalTo(pillar2Id))
        )
      }

      "return json when the backend has returned 200 OK with data" in {
        given hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("X-Pillar2-Id" -> pillar2Id)
        stubRequestWithPillar2Id("GET", readSubscriptionV2Url, OK, subscriptionSuccessV2Json)

        val result = await(subscriptionConnectorV2.readSubscription(plrReference))

        result.isRight mustBe true
        result mustBe Right(subscriptionDataV2)

        server.verify(
          getRequestedFor(urlEqualTo(readSubscriptionV2Url)).withHeader("X-Pillar2-Id", equalTo(pillar2Id))
        )
      }

      "return BadRequest when V2 is enabled but ETMP returns a V1-shaped body" in {
        given hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("X-Pillar2-Id" -> pillar2Id)
        stubRequestWithPillar2Id("GET", readSubscriptionV2Url, OK, subscriptionSuccessJson)

        await(subscriptionConnectorV2.readSubscription(plrReference)) mustBe Left(BadRequest)
      }

      "return BadRequest when ETMP returns non-200" in {
        given hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("X-Pillar2-Id" -> pillar2Id)
        stubRequestWithPillar2Id("GET", readSubscriptionV2Url, BAD_REQUEST, invalidSubscriptionJson)

        val result = await(subscriptionConnectorV2.readSubscription(plrReference))

        result.isLeft mustBe true
        result mustBe Left(BadRequest)
      }
    }
  }

}
