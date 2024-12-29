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
import uk.gov.hmrc.pillar2submissionapi.UnitTestBaseSpec
import uk.gov.hmrc.pillar2submissionapi.connectors.UKTaxReturnConnectorSpec.validUktrSubmission
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions._

import java.time.LocalDate
import java.time.temporal.ChronoUnit

class UKTaxReturnConnectorSpec extends UnitTestBaseSpec {

  lazy val ukTaxReturnConnector: UKTaxReturnConnector = app.injector.instanceOf[UKTaxReturnConnector]
  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(
      Configuration(
        "microservice.services.pillar2.port" -> server.port()
      )
    )
    .build()

  "UktrSubmissionConnector" when {
    "submitUktr() called with a valid request" must {
      "return 201 CREATED response" in {
        stubResponse("/report-pillar2-top-up-taxes/submit-uk-tax-return", CREATED, JsObject.empty)

        val result = await(ukTaxReturnConnector.submitUktr(validUktrSubmission)(hc))

        result.status should be(201)
      }
    }

    "submitUktr() called with an invalid request" must {
      "return 400 BAD_REQUEST response" in {
        stubResponse("/report-pillar2-top-up-taxes/submit-uk-tax-return", BAD_REQUEST, JsObject.empty)

        val result = await(ukTaxReturnConnector.submitUktr(validUktrSubmission)(hc))

        result.status should be(400)
      }
    }

    "submitUktr() called with an invalid url configured" must {
      "return 404 NOT_FOUND response" in {
        stubResponse("/INCORRECT_URL", CREATED, JsObject.empty)

        val result = await(ukTaxReturnConnector.submitUktr(validUktrSubmission)(hc))

        result.status should be(404)
      }
    }
  }
}

object UKTaxReturnConnectorSpec {

  val liableEntity: LiableEntity = LiableEntity("entityName", "idType", "idValue", 1.1, 2.2, 3.3)
  val liability: LiabilityData =
    LiabilityData(electionDTTSingleMember = true, electionUTPRSingleMember = false, 1, 2, 3.3, 4.4, 5.5, 6.6, Seq(liableEntity))
  val validUktrSubmission: UKTRSubmission = new UKTRSubmissionData(LocalDate.now(), LocalDate.now().plus(10, ChronoUnit.DAYS), true, true, liability)

}
