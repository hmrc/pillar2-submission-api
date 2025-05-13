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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.base.ControllerBaseSpec
import uk.gov.hmrc.pillar2submissionapi.controllers.error._
import uk.gov.hmrc.pillar2submissionapi.controllers.submission.OverseasReturnNotificationController
import uk.gov.hmrc.pillar2submissionapi.helpers.ORNDataFixture
import uk.gov.hmrc.pillar2submissionapi.models.overseasreturnnotification.ORNSubmission
import uk.gov.hmrc.pillar2submissionapi.models.response.Pillar2ErrorResponse

import scala.concurrent.Future

class OverseasReturnNotificationControllerSpec extends ControllerBaseSpec with ORNDataFixture {

  val ornController: OverseasReturnNotificationController =
    new OverseasReturnNotificationController(
      cc,
      identifierAction,
      pillar2IdAction,
      subscriptionDataRetrievalAction,
      mockOverseasReturnNotificationService
    )

  def callWithBody(jsRequest: JsValue): Future[Result] = ornController.submitORN(
    FakeRequest()
      .withJsonBody(jsRequest)
      .withHeaders("X-Pillar2-Id" -> pillar2Id)
  )

  def callAmendWithBody(jsRequest: JsValue): Future[Result] = ornController.amendORN(
    FakeRequest()
      .withJsonBody(jsRequest)
      .withHeaders("X-Pillar2-Id" -> pillar2Id)
  )

  "OverseasReturnNotificationController" when {
    "submitORN() called with a valid request" should {
      "return 201 CREATED response" in {

        when(mockOverseasReturnNotificationService.submitORN(any[ORNSubmission])(any[HeaderCarrier]))
          .thenReturn(
            Future.successful(
              submitOrnResponse
            )
          )

        status(callWithBody(ornRequestJs)) mustEqual CREATED
      }
    }

    "submitORN called with an invalid request" should {
      "return InvalidJson response" in {
        callWithBody(invalidRequestJson_data) shouldFailWith InvalidJson
      }
    }

    "submitORN called with an invalid json request" should {
      "return InvalidJson response" in {
        callWithBody(invalidRequest_Json) shouldFailWith InvalidJson
      }
    }

    "submitORN called with an empty json object" should {
      "return InvalidJson response" in {
        callWithBody(invalidRequest_emptyBody) shouldFailWith InvalidJson
      }
    }

    "submitORN called without X-Pillar2-Id" should {
      "return MissingHeader response" in {
        ornController.submitORN(
          FakeRequest()
        ) shouldFailWith MissingHeader.MissingPillar2Id
      }
    }

    "submitORN called with an non-json request" should {
      "return EmptyRequestBody response" in {
        val result: Future[Result] = ornController.submitORN(
          FakeRequest()
            .withTextBody(invalidRequest_wrongType)
            .withHeaders("X-Pillar2-Id" -> pillar2Id)
        )
        result shouldFailWith EmptyRequestBody
      }
    }

    "submitORN called with no request body" should {
      "return EmptyRequestBody response" in {
        val result: Future[Result] = ornController.submitORN(
          FakeRequest().withHeaders("X-Pillar2-Id" -> pillar2Id)
        )
        result shouldFailWith EmptyRequestBody
      }
    }

    "submitORN called with valid request body that contains duplicate entries" should {
      "return 201 CREATED response" in {
        status(callWithBody(validRequestJson_duplicateFields)) mustEqual CREATED
      }
    }

    "submitORN called with valid request body that contains additional fields" should {
      "return 201 CREATED response" in {
        status(callWithBody(validRequestJson_additionalFields)) mustEqual CREATED
      }
    }

    "submitORN called with invalid field lengths" should {

      "return InvalidJson response when countryGIR is longer than 2 characters" in {
        callWithBody(invalidCountryGIRJson) shouldFailWith InvalidJson
      }

      "return InvalidJson response when issuingCountryTIN is longer than 2 characters" in {
        callWithBody(invalidIssuingCountryTINJson) shouldFailWith InvalidJson
      }

      "return InvalidJson response when reportingEntityName is empty" in {
        callWithBody(invalidReportingEntityNameJson) shouldFailWith InvalidJson
      }

      "return InvalidJson response when TIN is empty" in {
        callWithBody(invalidTinJson) shouldFailWith InvalidJson
      }

      "return InvalidJson response when reportingEntityName exceeds 200 characters" in {
        callWithBody(invalidLongReportingEntityJson) shouldFailWith InvalidJson
      }

      "return InvalidJson response when TIN exceeds 200 characters" in {
        callWithBody(invalidLongTinJson) shouldFailWith InvalidJson
      }
    }

    "amendORN() called with a valid request" should {
      "return 200 OK response" in {

        when(mockOverseasReturnNotificationService.amendORN(any[ORNSubmission])(any[HeaderCarrier]))
          .thenReturn(
            Future.successful(
              submitOrnResponse
            )
          )
        status(callAmendWithBody(ornRequestJs)) mustEqual OK
      }
    }

    "amendORN called with an invalid request" should {
      "return InvalidJson response" in {
        callAmendWithBody(invalidRequestJson_data) shouldFailWith InvalidJson
      }
    }

    "amendORN called with an invalid json request" should {
      "return InvalidJson response" in {
        callAmendWithBody(invalidRequest_Json) shouldFailWith InvalidJson
      }
    }

    "amendORN called with an empty json object" should {
      "return InvalidJson response" in {
        callAmendWithBody(invalidRequest_emptyBody) shouldFailWith InvalidJson
      }
    }

    "amendORN called without X-Pillar2-Id" should {
      "return MissingHeader response" in {
        ornController.amendORN(
          FakeRequest()
        ) shouldFailWith MissingHeader.MissingPillar2Id
      }
    }

    "amendORN called with an non-json request" should {
      "return EmptyRequestBody response" in {
        val result: Future[Result] = ornController.amendORN(
          FakeRequest()
            .withTextBody(invalidRequest_wrongType)
            .withHeaders("X-Pillar2-Id" -> pillar2Id)
        )
        result shouldFailWith EmptyRequestBody
      }
    }

    "amendORN called with no request body" should {
      "return EmptyRequestBody response" in {
        val result: Future[Result] = ornController.amendORN(
          FakeRequest().withHeaders("X-Pillar2-Id" -> pillar2Id)
        )
        result shouldFailWith EmptyRequestBody
      }
    }

    "amendORN called with valid request body that contains duplicate entries" should {
      "return 200 OK response" in {
        status(callAmendWithBody(validRequestJson_duplicateFields)) mustEqual OK
      }
    }

    "amendORN called with valid request body that contains additional fields" should {
      "return 201 CREATED response" in {
        status(callAmendWithBody(validRequestJson_additionalFields)) mustEqual OK
      }
    }

    "amendORN called with invalid field lengths" should {

      "return InvalidJson response when countryGIR is longer than 2 characters" in {
        callAmendWithBody(invalidCountryGIRJson) shouldFailWith InvalidJson
      }

      "return InvalidJson response when issuingCountryTIN is longer than 2 characters" in {
        callAmendWithBody(invalidIssuingCountryTINJson) shouldFailWith InvalidJson
      }

      "return InvalidJson response when reportingEntityName is empty" in {
        callAmendWithBody(invalidReportingEntityNameJson) shouldFailWith InvalidJson
      }

      "return InvalidJson response when TIN is empty" in {
        callAmendWithBody(invalidTinJson) shouldFailWith InvalidJson
      }

      "return InvalidJson response when reportingEntityName exceeds 200 characters" in {
        callAmendWithBody(invalidLongReportingEntityJson) shouldFailWith InvalidJson
      }

      "return InvalidJson response when TIN exceeds 200 characters" in {
        callAmendWithBody(invalidLongTinJson) shouldFailWith InvalidJson
      }
    }

    "retrieveORN() called with valid parameters" should {
      "return 200 OK response" in {
        when(mockOverseasReturnNotificationService.retrieveORN(any[String], any[String])(any[HeaderCarrier]))
          .thenReturn(
            Future.successful(
              retrieveOrnResponse
            )
          )

        val request = FakeRequest()
          .withHeaders("X-Pillar2-Id" -> pillar2Id)

        val result = ornController.retrieveORN("2024-01-01", "2024-12-31")(request)

        status(result) mustEqual OK
      }
    }

    "retrieveORN() when no ORN exists" should {
      "return 404 NOT_FOUND with correct error code and message" in {
        when(mockOverseasReturnNotificationService.retrieveORN(any[String], any[String])(any[HeaderCarrier]))
          .thenReturn(
            Future.failed(ResourceNotFoundException)
          )

        val request = FakeRequest()
          .withHeaders("X-Pillar2-Id" -> pillar2Id)

        val result = ornController.retrieveORN("2024-01-01", "2024-12-31")(request)

        status(result) mustEqual NOT_FOUND
        contentAsJson(result) mustEqual Json.toJson(Pillar2ErrorResponse("RESOURCE_NOT_FOUND", "Not Found"))
      }
    }

    "retrieveORN() with downstream validation error" should {
      "return 422 UNPROCESSABLE_ENTITY with correct error code and message" in {
        when(mockOverseasReturnNotificationService.retrieveORN(any[String], any[String])(any[HeaderCarrier]))
          .thenReturn(
            Future.failed(DownstreamValidationError("093", "Invalid Return"))
          )

        val request = FakeRequest()
          .withHeaders("X-Pillar2-Id" -> pillar2Id)

        val result = ornController.retrieveORN("2024-01-01", "2024-12-31")(request)

        status(result) mustEqual UNPROCESSABLE_ENTITY
        contentAsJson(result) mustEqual Json.toJson(Pillar2ErrorResponse("093", "Invalid Return"))
      }
    }

    "retrieveORN() with unexpected error" should {
      "return 500 INTERNAL_SERVER_ERROR with correct error code and message" in {
        when(mockOverseasReturnNotificationService.retrieveORN(any[String], any[String])(any[HeaderCarrier]))
          .thenReturn(
            Future.failed(UnexpectedResponse)
          )

        val request = FakeRequest()
          .withHeaders("X-Pillar2-Id" -> pillar2Id)

        val result = ornController.retrieveORN("2024-01-01", "2024-12-31")(request)

        status(result) mustEqual INTERNAL_SERVER_ERROR
        contentAsJson(result) mustEqual Json.toJson(Pillar2ErrorResponse("500", "Internal Server Error"))
      }
    }
  }
}
