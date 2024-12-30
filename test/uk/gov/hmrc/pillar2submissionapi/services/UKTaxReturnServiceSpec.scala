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
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.base.UnitTestBaseSpec
import uk.gov.hmrc.pillar2submissionapi.controllers.error._
import uk.gov.hmrc.pillar2submissionapi.helpers.UKTRErrorCodes.INVALID_RETURN_093
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions._
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.responses.UKTRSubmitErrorResponse

import scala.concurrent.Future

class UKTaxReturnServiceSpec extends UnitTestBaseSpec {

  val mockUkTaxReturnService: UKTaxReturnService = new UKTaxReturnService(mockUKTaxReturnConnector)

  "UKTaxReturnService" when {
    "submitUKTR() is called with a UKTRSubmission" should {
      "forward the X-Pillar2-Id header" in {
        val captor = ArgumentCaptor.forClass(classOf[HeaderCarrier])
        when(mockUKTaxReturnConnector.submitUKTR(any[UKTRSubmission])(captor.capture()))
          .thenReturn(Future.successful(HttpResponse.apply(CREATED, Json.toJson(uktrSubmissionSuccessResponse), Map.empty)))

        val result =
          await(mockUkTaxReturnService.submitUKTR(validLiabilitySubmission)(hc = hc.withExtraHeaders("X-Pillar2-Id" -> pillar2Id)))

        assertEquals(uktrSubmissionSuccessResponse, result)
        captor.getValue.extraHeaders.map(_._1) must contain("X-Pillar2-Id")
        captor.getValue.extraHeaders.map(_._2).head mustEqual pillar2Id
      }
    }

    "submitUKTR() called with a valid tax return" should {
      "return 201 CREATED response" in {
        when(mockUKTaxReturnConnector.submitUKTR(any[UKTRSubmissionData])(any[HeaderCarrier]))
          .thenReturn(Future.successful(HttpResponse.apply(CREATED, Json.toJson(uktrSubmissionSuccessResponse), Map.empty)))

        val result = await(mockUkTaxReturnService.submitUKTR(validNilSubmission))

        assertEquals(uktrSubmissionSuccessResponse, result)
      }
    }

    "submitUKTR() called with a valid nil return" should {
      "return 201 CREATED response" in {
        when(mockUKTaxReturnConnector.submitUKTR(any[UKTRSubmissionNilReturn])(any[HeaderCarrier]))
          .thenReturn(Future.successful(HttpResponse.apply(CREATED, Json.toJson(uktrSubmissionSuccessResponse), Map.empty)))

        val result = await(mockUkTaxReturnService.submitUKTR(validNilSubmission))

        assertEquals(uktrSubmissionSuccessResponse, result)
      }
    }

    "submitUKTR() unparsable 201 response back" should {
      "Runtime exception thrown" in {
        when(mockUKTaxReturnConnector.submitUKTR(any[UKTRSubmissionData])(any[HeaderCarrier]))
          .thenReturn(Future.successful(HttpResponse.apply(CREATED, Json.toJson("unparsable success response"), Map.empty)))

        intercept[UnparsableResponse](await(mockUkTaxReturnService.submitUKTR(validNilSubmission)))
      }
    }

    "submitUKTR() valid 422 response back" should {
      "Runtime exception thrown (To be updated to the appropriate exception)" in {
        when(mockUKTaxReturnConnector.submitUKTR(any[UKTRSubmissionData])(any[HeaderCarrier]))
          .thenReturn(
            Future.successful(
              HttpResponse.apply(UNPROCESSABLE_ENTITY, Json.toJson(UKTRSubmitErrorResponse(INVALID_RETURN_093, "Invalid Return")), Map.empty)
            )
          )

        intercept[UktrValidationError](await(mockUkTaxReturnService.submitUKTR(validNilSubmission)))
      }
    }

    "submitUKTR() unparsable 422 response back" should {
      "Runtime exception thrown (To be updated to 500 Internal server error exception)" in {
        when(mockUKTaxReturnConnector.submitUKTR(any[UKTRSubmissionData])(any[HeaderCarrier]))
          .thenReturn(Future.successful(HttpResponse.apply(UNPROCESSABLE_ENTITY, Json.toJson("unparsable error response"), Map.empty)))

        intercept[UnparsableResponse](await(mockUkTaxReturnService.submitUKTR(validLiabilitySubmission)))
      }
    }

    "submitUKTR() 500 response back" should {
      "Runtime exception thrown " in {
        when(mockUKTaxReturnConnector.submitUKTR(any[UKTRSubmissionData])(any[HeaderCarrier]))
          .thenReturn(Future.successful(HttpResponse.apply(INTERNAL_SERVER_ERROR, Json.toJson(InternalServerError.toString()), Map.empty)))

        intercept[UnexpectedResponse.type](await(mockUkTaxReturnService.submitUKTR(validLiabilitySubmission)))
      }
    }
  }
}
