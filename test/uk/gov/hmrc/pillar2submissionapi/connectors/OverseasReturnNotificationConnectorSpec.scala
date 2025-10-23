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

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsObject
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import play.api.{Application, Configuration}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.base.UnitTestBaseSpec
import uk.gov.hmrc.pillar2submissionapi.helpers.ORNDataFixture

class OverseasReturnNotificationConnectorSpec extends UnitTestBaseSpec with ORNDataFixture {

  lazy val ornConnector: OverseasReturnNotificationConnector = app.injector.instanceOf[OverseasReturnNotificationConnector]
  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(Configuration("microservice.services.pillar2.port" -> server.port()))
    .build()

  private val submitUrl = "/report-pillar2-top-up-taxes/overseas-return-notification/submit"
  private val amendUrl  = "/report-pillar2-top-up-taxes/overseas-return-notification/amend"

  "SubmitORNConnector" when {
    "submitORN" must {
      "forward the X-Pillar2-Id header" in {
        implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("X-Pillar2-Id" -> pillar2Id)
        stubRequestWithPillar2Id("POST", submitUrl, CREATED, JsObject.empty)

        val result = await(ornConnector.submitORN(ornRequestFixture))

        result.status should be(CREATED)
        server.verify(
          postRequestedFor(urlEqualTo(submitUrl)).withHeader("X-Pillar2-Id", equalTo(pillar2Id))
        )
      }

      "return 201 CREATED for valid request" in {
        stubRequest("POST", submitUrl, CREATED, JsObject.empty)

        val result = await(ornConnector.submitORN(ornRequestFixture)(hc))

        result.status should be(CREATED)
      }

      "return 400 BAD_REQUEST for invalid request" in {
        stubRequest("POST", submitUrl, BAD_REQUEST, JsObject.empty)

        val result = await(ornConnector.submitORN(ornRequestFixture)(hc))

        result.status should be(BAD_REQUEST)
      }

      "return 404 NOT_FOUND for incorrect URL" in {
        stubRequest("POST", "/INCORRECT_URL", NOT_FOUND, JsObject.empty)

        val result = await(ornConnector.submitORN(ornRequestFixture)(hc))

        result.status should be(NOT_FOUND)
      }
    }
  }

  "AmendORNConnector" when {
    "amendORN" must {
      "forward the X-Pillar2-Id header" in {
        implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("X-Pillar2-Id" -> pillar2Id)
        stubRequestWithPillar2Id("PUT", amendUrl, OK, JsObject.empty)

        val result = await(ornConnector.amendORN(ornRequestFixture))

        result.status should be(OK)
        server.verify(
          putRequestedFor(urlEqualTo(amendUrl)).withHeader("X-Pillar2-Id", equalTo(pillar2Id))
        )
      }

      "return 200 CREATED for valid request" in {
        stubRequest("PUT", amendUrl, OK, JsObject.empty)

        val result = await(ornConnector.amendORN(ornRequestFixture)(hc))

        result.status should be(OK)
      }

      "return 400 BAD_REQUEST for invalid request" in {
        stubRequest("PUT", amendUrl, BAD_REQUEST, JsObject.empty)

        val result = await(ornConnector.amendORN(ornRequestFixture)(hc))

        result.status should be(BAD_REQUEST)
      }

      "return 404 NOT_FOUND for incorrect URL" in {
        stubRequest("PUT", "/INCORRECT_URL", NOT_FOUND, JsObject.empty)

        val result = await(ornConnector.amendORN(ornRequestFixture)(hc))

        result.status should be(NOT_FOUND)
      }
    }
  }
}
