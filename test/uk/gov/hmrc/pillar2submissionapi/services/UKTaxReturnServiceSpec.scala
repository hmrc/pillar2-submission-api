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
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.UnitTestBaseSpec
import uk.gov.hmrc.pillar2submissionapi.controllers.error._
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions._
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.responses.UKTRSubmitErrorResponse
import uk.gov.hmrc.pillar2submissionapi.services.UKTaxReturnServiceSpec._

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import scala.concurrent.Future

class UKTaxReturnServiceSpec extends UnitTestBaseSpec {

  val mockUkTaxReturnService: UKTaxReturnService = new UKTaxReturnService(mockPillar2Connector)

  "SubmitUktrService" when {
    "submitUktr() called with a valid tax return" should {
      "return 201 CREATED response" in {

        when(mockPillar2Connector.submitUktr(any[UKTRSubmissionData])(any[HeaderCarrier]))
          .thenReturn(Future.successful(HttpResponse.apply(CREATED, Json.toJson(uktrSubmissionSuccessResponse), Map.empty)))

        val result = await(mockUkTaxReturnService.submitUktr(validUktrSubmission(liabilityData)))

        assertEquals(uktrSubmissionSuccessResponse, result)
      }
    }
  }

  "submitUktr() called with a valid nil return" should {
    "return 201 CREATED response" in {

      when(mockPillar2Connector.submitUktr(any[UKTRSubmissionData])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(CREATED, Json.toJson(uktrSubmissionSuccessResponse), Map.empty)))

      val result = await(mockUkTaxReturnService.submitUktr(validUktrSubmission(liabilityNilReturn)))

      assertEquals(uktrSubmissionSuccessResponse, result)
    }
  }

  "submitUktr() unparsable 201 response back" should {
    "Runtime exception thrown" in {

      when(mockPillar2Connector.submitUktr(any[UKTRSubmissionData])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(CREATED, Json.toJson("unparsable success response"), Map.empty)))

      intercept[UnparsableResponse](await(mockUkTaxReturnService.submitUktr(validUktrSubmission(liabilityNilReturn))))
    }
  }

  "submitUktr() valid 422 response back" should {
    "Runtime exception thrown (To be updated to the appropriate exception)" in {

      when(mockPillar2Connector.submitUktr(any[UKTRSubmissionData])(any[HeaderCarrier]))
        .thenReturn(
          Future.successful(HttpResponse.apply(UNPROCESSABLE_ENTITY, Json.toJson(UKTRSubmitErrorResponse("093", "Invalid Return")), Map.empty))
        )

      intercept[UktrValidationError](await(mockUkTaxReturnService.submitUktr(validUktrSubmission(liabilityNilReturn))))
    }
  }

  "submitUktr() unparsable 422 response back" should {
    "Runtime exception thrown (To be updated to 500 Internal server error exception)" in {

      when(mockPillar2Connector.submitUktr(any[UKTRSubmissionData])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(UNPROCESSABLE_ENTITY, Json.toJson("unparsable error response"), Map.empty)))

      intercept[UnparsableResponse](await(mockUkTaxReturnService.submitUktr(validUktrSubmission(liabilityNilReturn))))
    }
  }

  "submitUktr() 500 response back" should {
    "Runtime exception thrown " in {

      when(mockPillar2Connector.submitUktr(any[UKTRSubmissionData])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(INTERNAL_SERVER_ERROR, Json.toJson(InternalServerError.toString()), Map.empty)))

      intercept[UnexpectedResponse.type](await(mockUkTaxReturnService.submitUktr(validUktrSubmission(liabilityNilReturn))))
    }
  }
}

object UKTaxReturnServiceSpec {
  def validUktrSubmission(liability: Liability): UKTRSubmission =
    liability match {
      case data: LiabilityData =>
        new UKTRSubmissionData(LocalDate.now(), LocalDate.now().plus(10, ChronoUnit.DAYS), true, true, data)
      case nilReturn: LiabilityNilReturn =>
        new UKTRSubmissionNilReturn(LocalDate.now(), LocalDate.now().plus(10, ChronoUnit.DAYS), true, true, nilReturn)
    }
}
