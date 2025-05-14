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
import uk.gov.hmrc.pillar2submissionapi.base.UnitTestBaseSpec
import uk.gov.hmrc.pillar2submissionapi.controllers.error._
import uk.gov.hmrc.pillar2submissionapi.helpers.ORNDataFixture
import uk.gov.hmrc.pillar2submissionapi.models.overseasreturnnotification.{ORNErrorResponse, ORNSubmission}

import scala.concurrent.Future

class OverseasReturnNotificationServiceSpec extends UnitTestBaseSpec with ORNDataFixture {

  val ornService: OverseasReturnNotificationService = new OverseasReturnNotificationService(mockOverseasReturnNotificationConnector)

  "submitORNService" when {
    "submitORN() called with a valid tax return" should {
      "return 201 CREATED response" in {

        when(mockOverseasReturnNotificationConnector.submitORN(any[ORNSubmission])(any[HeaderCarrier]))
          .thenReturn(Future.successful(HttpResponse.apply(201, Json.toJson(submitOrnResponse), Map.empty)))

        val result = await(ornService.submitORN(ornRequestFixture))

        assertEquals(submitOrnResponse, result)
      }
    }
  }

  "submitORN() unexpected 201 response back" should {
    "Runtime exception thrown" in {

      when(mockOverseasReturnNotificationConnector.submitORN(any[ORNSubmission])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(201, Json.toJson("unexpected success response"), Map.empty)))

      intercept[UnexpectedResponse.type](await(ornService.submitORN(ornRequestFixture)))
    }
  }

  "submitORN() valid 422 response back" should {
    "Runtime exception thrown" in {

      when(mockOverseasReturnNotificationConnector.submitORN(any[ORNSubmission])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(422, Json.toJson(ORNErrorResponse("093", "Invalid Return")), Map.empty)))

      intercept[DownstreamValidationError](await(ornService.submitORN(ornRequestFixture)))
    }
  }

  "submitORN() unexpected 422 response back" should {
    "Runtime exception thrown" in {

      when(mockOverseasReturnNotificationConnector.submitORN(any[ORNSubmission])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(422, Json.toJson("unexpected error response"), Map.empty)))

      intercept[UnexpectedResponse.type](await(ornService.submitORN(ornRequestFixture)))
    }
  }

  "submitORN() 500 response back" should {
    "Runtime exception thrown " in {

      when(mockOverseasReturnNotificationConnector.submitORN(any[ORNSubmission])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(500, Json.toJson(InternalServerError.toString()), Map.empty)))

      intercept[UnexpectedResponse.type](await(ornService.submitORN(ornRequestFixture)))
    }
  }

  "amendORNService" when {
    "amendORN() called with a valid tax return" should {
      "return 200 OK response" in {

        when(mockOverseasReturnNotificationConnector.amendORN(any[ORNSubmission])(any[HeaderCarrier]))
          .thenReturn(Future.successful(HttpResponse.apply(200, Json.toJson(submitOrnResponse), Map.empty)))

        val result = await(ornService.amendORN(ornRequestFixture))

        assertEquals(submitOrnResponse, result)
      }
    }
  }

  "amendORN() unexpected 200 response back" should {
    "Runtime exception thrown" in {

      when(mockOverseasReturnNotificationConnector.amendORN(any[ORNSubmission])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(200, Json.toJson("unexpected success response"), Map.empty)))

      intercept[UnexpectedResponse.type](await(ornService.amendORN(ornRequestFixture)))
    }
  }

  "amendORN() valid 422 response back" should {
    "Runtime exception thrown" in {

      when(mockOverseasReturnNotificationConnector.amendORN(any[ORNSubmission])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(422, Json.toJson(ORNErrorResponse("093", "Invalid Return")), Map.empty)))

      intercept[DownstreamValidationError](await(ornService.amendORN(ornRequestFixture)))
    }
  }

  "amendORN() unexpected 422 response back" should {
    "Runtime exception thrown" in {

      when(mockOverseasReturnNotificationConnector.amendORN(any[ORNSubmission])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(422, Json.toJson("unexpected error response"), Map.empty)))

      intercept[UnexpectedResponse.type](await(ornService.amendORN(ornRequestFixture)))
    }
  }

  "amendORN() 500 response back" should {
    "Runtime exception thrown " in {

      when(mockOverseasReturnNotificationConnector.amendORN(any[ORNSubmission])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(500, Json.toJson(InternalServerError.toString()), Map.empty)))

      intercept[UnexpectedResponse.type](await(ornService.amendORN(ornRequestFixture)))
    }
  }

  "retrieveORNService" when {
    "retrieveORN() called with valid accounting periods" should {
      "return 200 OK response" in {
        when(mockOverseasReturnNotificationConnector.retrieveORN(any[String], any[String])(any[HeaderCarrier]))
          .thenReturn(Future.successful(HttpResponse.apply(200, Json.toJson(retrieveOrnResponse), Map.empty)))

        val result = await(ornService.retrieveORN("2024-01-01", "2024-12-31"))

        assertEquals(retrieveOrnResponse, result)
      }
    }
  }

  "retrieveORN() unexpected 200 response back" should {
    "Runtime exception thrown" in {
      when(mockOverseasReturnNotificationConnector.retrieveORN(any[String], any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(200, Json.toJson("unexpected success response"), Map.empty)))

      intercept[UnexpectedResponse.type](await(ornService.retrieveORN("2024-01-01", "2024-12-31")))
    }
  }

  "retrieveORN() 404 response back" should {
    "UnexpectedResponse thrown" in {
      when(mockOverseasReturnNotificationConnector.retrieveORN(any[String], any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(404, Json.toJson("Not Found"), Map.empty)))

      intercept[UnexpectedResponse.type](await(ornService.retrieveORN("2024-01-01", "2024-12-31")))
    }
  }

  "retrieveORN() 422 response with No Form bundle found" should {
    "ResourceNotFoundException thrown" in {
      when(mockOverseasReturnNotificationConnector.retrieveORN(any[String], any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(422, Json.toJson(ORNErrorResponse("005", "No Form Bundle found")), Map.empty)))

      intercept[ORNNotFoundException.type](await(ornService.retrieveORN("2024-01-01", "2024-12-31")))
    }
  }

  "retrieveORN() valid 422 response back" should {
    "DownstreamValidationError thrown" in {
      when(mockOverseasReturnNotificationConnector.retrieveORN(any[String], any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(422, Json.toJson(ORNErrorResponse("093", "Invalid Return")), Map.empty)))

      intercept[DownstreamValidationError](await(ornService.retrieveORN("2024-01-01", "2024-12-31")))
    }
  }

  "retrieveORN() unexpected 422 response back" should {
    "UnexpectedResponse thrown" in {
      when(mockOverseasReturnNotificationConnector.retrieveORN(any[String], any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(422, Json.toJson("unexpected error response"), Map.empty)))

      intercept[UnexpectedResponse.type](await(ornService.retrieveORN("2024-01-01", "2024-12-31")))
    }
  }

  "retrieveORN() 500 response back" should {
    "UnexpectedResponse thrown" in {
      when(mockOverseasReturnNotificationConnector.retrieveORN(any[String], any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(500, Json.toJson(InternalServerError.toString()), Map.empty)))

      intercept[UnexpectedResponse.type](await(ornService.retrieveORN("2024-01-01", "2024-12-31")))
    }
  }
}
