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

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.pillar2submissionapi.UnitTestBaseSpec
import uk.gov.hmrc.pillar2submissionapi.connectors.UktrSubmissionConnectorSpec.validUktrSubmission
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.{LiabilityData, LiableEntity, UktrSubmission, UktrSubmissionData}

import java.net.URL
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import scala.concurrent.Future
import uk.gov.hmrc.http.HttpReads.Implicits._


class UktrSubmissionConnectorSpec extends UnitTestBaseSpec {

  val uktrSubmissionConnector: Pillar2Connector = new Pillar2Connector(appConfig, mockHttpClient)
  val pillar2UktrSubmissionUrl: String           = appConfig.pillar2BaseUrl + "/UPDATE_THIS_URL"

  "UktrSubmissionConnector" when {
    "submitUktr() called with a valid request" must {
      "return 201 CREATED response" in {

        when(
          mockHttpClient
            .post(ArgumentMatchers.eq(new URL(pillar2UktrSubmissionUrl)))
            .withBody(Json.toJson(validUktrSubmission))
            .execute[HttpResponse]
        ).thenReturn(Future.successful(HttpResponse.apply(201, Created.body.toString)))

        val result = uktrSubmissionConnector.submitUktr(validUktrSubmission)(hc)

        result.map(rs => rs.status should be(201))
      }
    }
  }
}

object UktrSubmissionConnectorSpec {

  val liableEntity: LiableEntity = LiableEntity("entityName", "idType", "idValue", 1.1, 2.2, 3.3)
  val liability: LiabilityData =
    LiabilityData(electionDTTSingleMember = true, electionUTPRSingleMember = false, 1, 2, 3.3, 4.4, 5.5, 6.6, Seq(liableEntity))
  val validUktrSubmission: UktrSubmission = new UktrSubmissionData(LocalDate.now(), LocalDate.now().plus(10, ChronoUnit.DAYS), true, true, liability)

}
