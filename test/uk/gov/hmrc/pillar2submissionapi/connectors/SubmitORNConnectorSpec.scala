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
import uk.gov.hmrc.pillar2submissionapi.connectors.SubmitORNConnectorSpec.validORNSubmission
import uk.gov.hmrc.pillar2submissionapi.models.overseasreturnnotification.ORNSubmission

import java.time.LocalDate

class SubmitORNConnectorSpec extends UnitTestBaseSpec {

  lazy val submitORNConnector: SubmitORNConnector = app.injector.instanceOf[SubmitORNConnector]
  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(Configuration("microservice.services.pillar2.port" -> server.port()))
    .build()

  private val submitUrl = "/report-pillar2-top-up-taxes/overseas-return-notification/submit"

  "SubmitORNConnector" when {
    "submitORN" must {
      "forward the X-Pillar2-Id header" in {
        implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("X-Pillar2-Id" -> pillar2Id)
        stubRequestWithPillar2Id("POST", submitUrl, CREATED, JsObject.empty)

        val result = await(submitORNConnector.submitORN(validORNSubmission))

        result.status should be(CREATED)
        server.verify(
          postRequestedFor(urlEqualTo(submitUrl)).withHeader("X-Pillar2-Id", equalTo(pillar2Id))
        )
      }

      "return 201 CREATED for valid request" in {
        stubRequest("POST", submitUrl, CREATED, JsObject.empty)

        val result = await(submitORNConnector.submitORN(validORNSubmission)(hc))

        result.status should be(CREATED)
      }

      "return 400 BAD_REQUEST for invalid request" in {
        stubRequest("POST", submitUrl, BAD_REQUEST, JsObject.empty)

        val result = await(submitORNConnector.submitORN(validORNSubmission)(hc))

        result.status should be(BAD_REQUEST)
      }

      "return 404 NOT_FOUND for incorrect URL" in {
        stubRequest("POST", "/INCORRECT_URL", NOT_FOUND, JsObject.empty)

        val result = await(submitORNConnector.submitORN(validORNSubmission)(hc))

        result.status should be(NOT_FOUND)
      }
    }
  }
}

object SubmitORNConnectorSpec {
  val validORNSubmission: ORNSubmission = new ORNSubmission(
    accountingPeriodFrom = LocalDate.now(),
    accountingPeriodTo = LocalDate.now().plusYears(1),
    filedDateGIR = LocalDate.now().plusYears(1),
    countryGIR = "US",
    reportingEntityName = "Newco PLC",
    TIN = "US12345678",
    issuingCountryTIN = "US"
  )
}
