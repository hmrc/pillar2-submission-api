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

import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.http.Status.{BAD_REQUEST, CREATED}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsObject
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import play.api.{Application, Configuration}
import uk.gov.hmrc.pillar2submissionapi.base.UnitTestBaseSpec
import uk.gov.hmrc.pillar2submissionapi.connectors.SubmitBTNConnectorSpec.validBTNSubmission
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.BTNSubmission

import java.time.LocalDate
import java.time.temporal.ChronoUnit

class SubmitBTNConnectorSpec extends UnitTestBaseSpec {

  lazy val submitBTNConnector: SubmitBTNConnector = app.injector.instanceOf[SubmitBTNConnector]
  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(
      Configuration(
        "microservice.services.pillar2.port" -> server.port()
      )
    )
    .build()

  "SubmitBTNConnector" when {
    "submitBTN() called with a valid request" must {
      "return 201 CREATED response" in {
        stubResponse("/report-pillar2-top-up-taxes/below-threshold-notification/submit", CREATED, JsObject.empty)

        val result = await(submitBTNConnector.submitBTN(validBTNSubmission)(hc))

        result.status should be(201)
      }
    }

    "submitBTN() called with an invalid request" must {
      "return 400 BAD_REQUEST response" in {
        stubResponse("/report-pillar2-top-up-taxes/below-threshold-notification/submit", BAD_REQUEST, JsObject.empty)

        val result = await(submitBTNConnector.submitBTN(validBTNSubmission)(hc))

        result.status should be(400)
      }
    }

    "submitBTN() called with an invalid url configured" must {
      "return 404 NOT_FOUND response" in {

        val result = await(submitBTNConnector.submitBTN(validBTNSubmission)(hc))

        result.status should be(404)
      }
    }
  }
}

object SubmitBTNConnectorSpec {
  val validBTNSubmission: BTNSubmission = new BTNSubmission(LocalDate.now(), LocalDate.now().plus(365, ChronoUnit.DAYS))

}
