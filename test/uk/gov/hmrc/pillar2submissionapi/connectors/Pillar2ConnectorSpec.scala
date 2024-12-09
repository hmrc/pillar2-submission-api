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
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Configuration, inject}
import play.api.http.Status.CREATED
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsObject
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.UnitTestBaseSpec
import uk.gov.hmrc.pillar2submissionapi.connectors.Pillar2ConnectorSpec.validUktrSubmission
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.{LiabilityData, LiableEntity, UktrSubmission, UktrSubmissionData}

import java.time.LocalDate
import java.time.temporal.ChronoUnit

class Pillar2ConnectorSpec extends UnitTestBaseSpec {

  server.start()
  override def fakeApplication() = new GuiceApplicationBuilder()
    .configure(
      Configuration(
        "microservice.services.create-subscription.port" -> server.port()
      )
    ).overrides(inject.bind[HeaderCarrier].to(HeaderCarrier()))
    .build()

  "UktrSubmissionConnector" when {
    "submitUktr() called with a valid request" must {
      "return 201 CREATED response" in {

        val uktrSubmissionConnector: Pillar2Connector = app.injector.instanceOf[Pillar2Connector]
        stubResponse("http://localhost:10051/UPDATE_THIS_URL", CREATED, JsObject.empty)

        val result = uktrSubmissionConnector.submitUktr(validUktrSubmission)(hc)

        result.map(rs => rs.status should be(201))
      }
    }
  }
}

object Pillar2ConnectorSpec {

  val liableEntity: LiableEntity = LiableEntity("entityName", "idType", "idValue", 1.1, 2.2, 3.3)
  val liability: LiabilityData =
    LiabilityData(electionDTTSingleMember = true, electionUTPRSingleMember = false, 1, 2, 3.3, 4.4, 5.5, 6.6, Seq(liableEntity))
  val validUktrSubmission: UktrSubmission = new UktrSubmissionData(LocalDate.now(), LocalDate.now().plus(10, ChronoUnit.DAYS), true, true, liability)

}
