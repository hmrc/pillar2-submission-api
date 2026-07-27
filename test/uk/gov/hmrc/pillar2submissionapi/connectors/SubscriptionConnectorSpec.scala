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

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalTo, get, getRequestedFor, urlEqualTo}
import com.github.tomakehurst.wiremock.http.Fault
import org.scalatest.matchers.should.Matchers.should
import play.api.http.Status.*
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsObject
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import play.api.{Application, Configuration}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.base.UnitTestBaseSpec
import uk.gov.hmrc.pillar2submissionapi.fixtures.SubscriptionDataFixtures
import uk.gov.hmrc.pillar2submissionapi.models.error.Pillar2Error.UnexpectedResponseError

class SubscriptionConnectorSpec extends UnitTestBaseSpec with SubscriptionDataFixtures {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(Configuration("microservice.services.pillar2.port" -> server.port()))
    .build()

  lazy val subscriptionConnector: SubscriptionConnector = app.injector.instanceOf[SubscriptionConnector]

  private val plrReference = "XAPLR0000000001"

  private val readSubscriptionUrl = s"$readSubscriptionPath/$plrReference"

  private val invalidSubscriptionJson: JsObject = JsObject.empty

  "SubscriptionConnector.readSubscription" must {
    "forward the X-Pillar2-Id header" in {
      given hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("X-Pillar2-Id" -> testPillar2Id)
      stubRequestWithPillar2Id("GET", readSubscriptionUrl, OK, subscriptionSuccessJson)

      val result = await(subscriptionConnector.readSubscription(plrReference))

      result.isRight mustBe true
      server.verify(
        getRequestedFor(urlEqualTo(readSubscriptionUrl)).withHeader("X-Pillar2-Id", equalTo(testPillar2Id))
      )
    }

    "return json when the backend has returned 200 OK with data" in {
      given hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("X-Pillar2-Id" -> testPillar2Id)
      stubRequestWithPillar2Id("GET", readSubscriptionUrl, OK, subscriptionSuccessJson)

      val result = await(subscriptionConnector.readSubscription(plrReference))

      result.isRight mustBe true
      result mustBe Right(subscriptionData)

      server.verify(
        getRequestedFor(urlEqualTo(readSubscriptionUrl)).withHeader("X-Pillar2-Id", equalTo(testPillar2Id))
      )
    }

    "return BadRequest ETMP returns non-parseable JSON" in {
      given hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("X-Pillar2-Id" -> testPillar2Id)
      stubRequestWithPillar2Id("GET", readSubscriptionUrl, OK, invalidSubscriptionJson)

      await(subscriptionConnector.readSubscription(plrReference)) mustBe Left(BadRequest)
    }

    "return BadRequest when ETMP returns non-200" in {
      given hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("X-Pillar2-Id" -> testPillar2Id)
      stubRequestWithPillar2Id("GET", readSubscriptionUrl, BAD_REQUEST, invalidSubscriptionJson)

      val result = await(subscriptionConnector.readSubscription(plrReference))

      result.isLeft mustBe true
      result mustBe Left(BadRequest)
    }

    "return UnexpectedResponseError when the request fails" in {
      given hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("X-Pillar2-Id" -> testPillar2Id)

      server.stubFor(
        get(urlEqualTo(readSubscriptionUrl))
          .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))
      )

      val result = await(subscriptionConnector.readSubscription(plrReference).failed)

      result should be(UnexpectedResponseError)
    }
  }

}
