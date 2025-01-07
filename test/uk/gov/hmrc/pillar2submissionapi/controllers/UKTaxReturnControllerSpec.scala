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

package uk.gov.hmrc.pillar2submissionapi.controllers

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status.CREATED
import play.api.libs.json.JsValue
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.base.ControllerBaseSpec
import uk.gov.hmrc.pillar2submissionapi.controllers.error.{EmptyRequestBody, InvalidJson}
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.UKTRSubmissionData

import scala.concurrent.Future

class UKTaxReturnControllerSpec extends ControllerBaseSpec {

  val uktrSubmissionController: UKTaxReturnController =
    new UKTaxReturnController(cc, identifierAction, subscriptionAction, mockUkTaxReturnService)(ec)

  def callWithBody(request: JsValue): Future[Result] = uktrSubmissionController.submitUKTR()(FakeRequest().withJsonBody(request))

  "UktrSubmissionController" when {
    "submitUKTR() called with a valid request" should {
      "return 201 CREATED response" in {
        when(mockUkTaxReturnService.submitUKTR(any[UKTRSubmissionData])(any[HeaderCarrier]))
          .thenReturn(Future.successful(uktrSubmissionSuccessResponse))

        status(of = callWithBody(validLiabilityReturn)) mustEqual CREATED
      }

      "forward the X-Pillar2-Id header" in {
        val captor = ArgumentCaptor.forClass(classOf[HeaderCarrier])
        when(mockUkTaxReturnService.submitUKTR(any[UKTRSubmissionData])(captor.capture()))
          .thenReturn(Future.successful(uktrSubmissionSuccessResponse))

        status(of = callWithBody(validLiabilityReturn)) mustEqual CREATED
        captor.getValue.extraHeaders.map(_._1) must contain("X-Pillar2-Id")
      }
    }

    "submitUKTR() called with a valid nil return request" should {
      "return 201 CREATED response" in {
        when(mockUkTaxReturnService.submitUKTR(any[UKTRSubmissionData])(any[HeaderCarrier]))
          .thenReturn(Future.successful(uktrSubmissionSuccessResponse))

        status(of = callWithBody(validNilReturn)) mustEqual CREATED
      }

      "forward the X-Pillar2-Id header" in {
        val captor = ArgumentCaptor.forClass(classOf[HeaderCarrier])
        when(mockUkTaxReturnService.submitUKTR(any[UKTRSubmissionData])(captor.capture()))
          .thenReturn(Future.successful(uktrSubmissionSuccessResponse))

        status(of = callWithBody(validNilReturn)) mustEqual CREATED
        captor.getValue.extraHeaders.map(_._1) must contain("X-Pillar2-Id")
      }
    }

    "submitUKTR() called with an invalid request" should {
      "return 400 BAD_REQUEST response" in {
        callWithBody(liabilityReturnInvalidLiabilities) shouldFailWith InvalidJson
      }
    }

    "submitUKTR() called with an invalid nil return request" should {
      "return 400 BAD_REQUEST response" in {

        callWithBody(nilReturnInvalidReturnType) shouldFailWith InvalidJson
      }
    }

    "submitUKTR() called with an invalid json request" should {
      "return 400 BAD_REQUEST response" in {
        callWithBody(invalidBody) shouldFailWith InvalidJson
      }
    }

    "submitUKTR() called with request that only contains a valid return type" should {
      "return 400 BAD_REQUEST response" in {

        callWithBody(invalidRequest_nilReturn_onlyContainsLiabilities) shouldFailWith InvalidJson
      }
    }

    "submitUKTR() called with request that only contains an invalid return type" should {
      "return 400 BAD_REQUEST response" in {
        callWithBody(invalidRequest_nilReturn_onlyLiabilitiesButInvalidReturnType) shouldFailWith InvalidJson
      }
    }

    "submitUKTR() called with request that is missing liabilities" should {
      "return 400 BAD_REQUEST response" in {
        callWithBody(invalidRequest_noLiabilities) shouldFailWith InvalidJson
      }
    }

    "submitUKTR() called with an empty json object" should {
      "return 400 BAD_REQUEST response" in {
        callWithBody(emptyBody) shouldFailWith InvalidJson
      }
    }

    "submitUKTR() called with an non-json request" should {
      "return 400 BAD_REQUEST response" in {
        val result = uktrSubmissionController.submitUKTR()(FakeRequest().withTextBody(stringBody))

        result shouldFailWith EmptyRequestBody
      }
    }

    "submitUKTR() called with no request body" should {
      "return 400 BAD_REQUEST response" in {
        val result = uktrSubmissionController.submitUKTR()(FakeRequest())

        result shouldFailWith EmptyRequestBody
      }
    }

    "submitUKTR() called with valid request body that contains duplicate entries" should {
      "return 201 CREATED response" in {
        status(of = callWithBody(liabilityReturnDuplicateFields)) mustEqual CREATED
      }
    }

    "submitUKTR() called with valid request body that contains additional fields" should {
      "return 201 CREATED response" in {
        status(of = callWithBody(liabilityReturnWithadditionalFields)) mustEqual CREATED
      }
    }

    "submitUKTR() called with valid request body that contains both a full and a nil submission" should {
      "return 201 CREATED response" in {
        status(of = callWithBody(liabilityAndNilReturn)) mustEqual CREATED
      }
    }
  }
}
