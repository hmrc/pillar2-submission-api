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

import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, postRequestedFor, urlEqualTo}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.http.Status.{BAD_REQUEST, CREATED, NOT_FOUND}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsObject
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import play.api.{Application, Configuration}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.base.UnitTestBaseSpec

class UKTaxReturnConnectorSpec extends UnitTestBaseSpec {

  lazy val ukTaxReturnConnector: UKTaxReturnConnector = app.injector.instanceOf[UKTaxReturnConnector]
  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(Configuration("microservice.services.pillar2.port" -> server.port()))
    .build()

  "UKTaxReturnConnector" when {
    "submitUktr() called with a valid request" must {
      "forward the X-Pillar2-Id header" in {
        implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("X-Pillar2-Id" -> pillar2Id)
        stubResponseWithExtraHeader("/report-pillar2-top-up-taxes/submit-uk-tax-return", CREATED, JsObject.empty)

        val result = await(ukTaxReturnConnector.submitUktr(validLiabilitySubmission))

        result.status should be(CREATED)
        server.verify(
          postRequestedFor(urlEqualTo("/report-pillar2-top-up-taxes/submit-uk-tax-return")).withHeader("X-Pillar2-Id", equalTo(pillar2Id))
        )
      }

      "return 201 CREATED response" in {
        stubResponse("/report-pillar2-top-up-taxes/submit-uk-tax-return", CREATED, JsObject.empty)

        val result = await(ukTaxReturnConnector.submitUktr(validLiabilitySubmission)(hc))

        result.status should be(CREATED)
      }
    }

    "submitUktr() called with an invalid request" must {
      "return 400 BAD_REQUEST response" in {
        stubResponse("/report-pillar2-top-up-taxes/submit-uk-tax-return", BAD_REQUEST, JsObject.empty)

        val result = await(ukTaxReturnConnector.submitUktr(validLiabilitySubmission)(hc))

        result.status should be(BAD_REQUEST)
      }
    }

    "submitUktr() called with an invalid url configured" must {
      "return 404 NOT_FOUND response" in {
        stubResponse("/INCORRECT_URL", CREATED, JsObject.empty)

        val result = await(ukTaxReturnConnector.submitUktr(validLiabilitySubmission)(hc))

        result.status should be(NOT_FOUND)
      }
    }
  }
}
