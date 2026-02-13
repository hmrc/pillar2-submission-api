/*
 * Copyright 2025 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.base.UnitTestBaseSpec
import uk.gov.hmrc.pillar2submissionapi.connectors.GIRConnector
import uk.gov.hmrc.pillar2submissionapi.controllers.error.{DownstreamValidationError, UnexpectedResponse}
import uk.gov.hmrc.pillar2submissionapi.models.globeinformationreturn.*

import java.time.LocalDate
import scala.concurrent.Future

class GIRServiceSpec extends UnitTestBaseSpec {

  val mockGIRConnector: GIRConnector = mock[GIRConnector]
  val service:          GIRService   = new GIRService(mockGIRConnector)

  val validSubmission: GIRSubmission            = GIRSubmission(LocalDate.parse("2024-01-01"), LocalDate.parse("2024-12-31"))
  val okResponse:      SubmitGIRSuccessResponse = SubmitGIRSuccessResponse(GIRSuccess("2024-01-01T12:00:00Z"))
  val errorResponse:   SubmitGIRErrorResponse   = SubmitGIRErrorResponse(GIRError("093", "Invalid Return"))

  "GIRService" when {
    "createGIR() called with a valid submission" should {
      "return 201 CREATED response" in {
        when(mockGIRConnector.createGIR(any[GIRSubmission])(using any[HeaderCarrier]))
          .thenReturn(Future.successful(HttpResponse.apply(201, Json.toJson(okResponse), Map.empty)))

        val result = await(service.createGIR(validSubmission))
        result mustBe okResponse
      }
    }
    "createGIR() valid 422 response back" should {
      "throw DownstreamValidationError" in {
        when(mockGIRConnector.createGIR(any[GIRSubmission])(using any[HeaderCarrier]))
          .thenReturn(Future.successful(HttpResponse.apply(422, Json.toJson(errorResponse), Map.empty)))

        intercept[DownstreamValidationError](await(service.createGIR(validSubmission)))
      }
    }
    "createGIR() unexpected 201 response back" should {
      "throw UnexpectedResponse" in {
        when(mockGIRConnector.createGIR(any[GIRSubmission])(using any[HeaderCarrier]))
          .thenReturn(Future.successful(HttpResponse.apply(201, Json.toJson("unexpected success response"), Map.empty)))

        intercept[UnexpectedResponse.type](await(service.createGIR(validSubmission)))
      }
    }
    "createGIR() unexpected 422 response back" should {
      "throw UnexpectedResponse" in {
        when(mockGIRConnector.createGIR(any[GIRSubmission])(using any[HeaderCarrier]))
          .thenReturn(Future.successful(HttpResponse.apply(422, Json.toJson("unexpected error response"), Map.empty)))

        intercept[UnexpectedResponse.type](await(service.createGIR(validSubmission)))
      }
    }
    "createGIR() 500 response back" should {
      "throw UnexpectedResponse" in {
        when(mockGIRConnector.createGIR(any[GIRSubmission])(using any[HeaderCarrier]))
          .thenReturn(Future.successful(HttpResponse.apply(500, Json.toJson("InternalServerError"), Map.empty)))

        intercept[UnexpectedResponse.type](await(service.createGIR(validSubmission)))
      }
    }
  }
}
