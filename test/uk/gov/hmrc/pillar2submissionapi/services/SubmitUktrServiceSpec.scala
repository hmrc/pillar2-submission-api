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

package uk.gov.hmrc.pillar2submissionapi.services

import junit.framework.TestCase.assertEquals
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.UnitTestBaseSpec
import uk.gov.hmrc.pillar2submissionapi.controllers.error._
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.ReturnType.NIL_RETURN
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions._
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.responses.{SubmitUktrErrorResponse, SubmitUktrSuccessResponse}
import uk.gov.hmrc.pillar2submissionapi.services.SubmitUktrServiceSpec._

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import scala.concurrent.Future

class SubmitUktrServiceSpec extends UnitTestBaseSpec {

  val submitUktrService: SubmitUktrService = new SubmitUktrService(mockPillar2Connector)

  "SubmitUktrService" when {
    "submitUktr() called with a valid tax return" should {
      "return 201 CREATED response" in {

        when(mockPillar2Connector.submitUktr(any[UktrSubmissionData])(any[HeaderCarrier]))
          .thenReturn(Future.successful(HttpResponse.apply(201, Json.toJson(okResponse), Map.empty)))

        val result = await(submitUktrService.submitUktr(validUktrSubmission(liabilityData)))

        assertEquals(okResponse, result)
      }
    }
  }

  "submitUktr() called with a valid nil return" should {
    "return 201 CREATED response" in {

      when(mockPillar2Connector.submitUktr(any[UktrSubmissionData])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(201, Json.toJson(okResponse), Map.empty)))

      val result = await(submitUktrService.submitUktr(validUktrSubmission(liabilityNilReturn)))

      assertEquals(okResponse, result)
    }
  }

  "submitUktr() unparsable 201 response back" should {
    "Runtime exception thrown" in {

      when(mockPillar2Connector.submitUktr(any[UktrSubmissionData])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(201, Json.toJson("unparsable success response"), Map.empty)))

      intercept[UnParsableResponse](await(submitUktrService.submitUktr(validUktrSubmission(liabilityNilReturn))))
    }
  }

  "submitUktr() valid 422 response back" should {
    "Runtime exception thrown (To be updated to the appropriate exception)" in {

      when(mockPillar2Connector.submitUktr(any[UktrSubmissionData])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(422, Json.toJson(SubmitUktrErrorResponse("093", "Invalid Return")), Map.empty)))

      intercept[UktrValidationError](await(submitUktrService.submitUktr(validUktrSubmission(liabilityNilReturn))))
    }
  }

  "submitUktr() unparsable 422 response back" should {
    "Runtime exception thrown (To be updated to 500 Internal server error exception)" in {

      when(mockPillar2Connector.submitUktr(any[UktrSubmissionData])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(422, Json.toJson("unparsable error response"), Map.empty)))

      intercept[UnParsableResponse](await(submitUktrService.submitUktr(validUktrSubmission(liabilityNilReturn))))
    }
  }

  "submitUktr() 500 response back" should {
    "Runtime exception thrown " in {

      when(mockPillar2Connector.submitUktr(any[UktrSubmissionData])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(500, Json.toJson(InternalServerError.toString()), Map.empty)))

      intercept[UnexpectedResponse.type](await(submitUktrService.submitUktr(validUktrSubmission(liabilityNilReturn))))
    }
  }
}

object SubmitUktrServiceSpec {
  val liableEntity: LiableEntity = LiableEntity("entityName", "idType", "idValue", 1.1, 2.2, 3.3)
  val liabilityData: LiabilityData =
    LiabilityData(electionDTTSingleMember = true, electionUTPRSingleMember = false, 1, 2, 3.3, 4.4, 5.5, 6.6, Seq(liableEntity))
  val liabilityNilReturn: LiabilityNilReturn = LiabilityNilReturn(NIL_RETURN)

  def validUktrSubmission(liability: Liability): UktrSubmission =
    liability match {
      case data: LiabilityData =>
        new UktrSubmissionData(LocalDate.now(), LocalDate.now().plus(10, ChronoUnit.DAYS), true, true, data)
      case nilReturn: LiabilityNilReturn =>
        new UktrSubmissionNilReturn(LocalDate.now(), LocalDate.now().plus(10, ChronoUnit.DAYS), true, true, nilReturn)
      case _ =>
        throw new RuntimeException("bad test data")
    }

  val okResponse: SubmitUktrSuccessResponse = SubmitUktrSuccessResponse("2022-01-31", "119000004320", Some("XTC01234123412"))
}
