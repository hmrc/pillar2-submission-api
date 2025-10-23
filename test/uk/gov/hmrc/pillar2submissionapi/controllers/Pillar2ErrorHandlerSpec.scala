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

import org.scalacheck.Gen
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers.mustEqual
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}
import uk.gov.hmrc.pillar2submissionapi.controllers.error._
import uk.gov.hmrc.pillar2submissionapi.models.response.Pillar2ErrorResponse

class Pillar2ErrorHandlerSpec extends AnyFunSuite with ScalaCheckDrivenPropertyChecks {

  val classUnderTest = new Pillar2ErrorHandler
  val dummyRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  test("client errors should be returned") {
    val validStatus = Gen.choose(400, 499)
    val messageGen  = Gen.alphaStr
    forAll(validStatus, messageGen) { (statusCode, message) =>
      val result = classUnderTest.onClientError(dummyRequest, statusCode, message)
      status(result) mustEqual statusCode
      val response = contentAsJson(result).as[Pillar2ErrorResponse]
      response.message mustEqual message
      response.code mustEqual (statusCode match {
        case 400 => "BAD_REQUEST"
        case 408 => "REQUEST_TIMEOUT"
        case 413 => "PAYLOAD_TOO_LARGE"
        case 415 => "UNSUPPORTED_MEDIA_TYPE"
        case _   => statusCode.toString
      })
    }
  }

  test("400 BAD_REQUEST error response") {
    val response = classUnderTest.onClientError(dummyRequest, 400, "Invalid request")
    status(response) mustEqual 400
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "BAD_REQUEST"
    result.message mustEqual "Invalid request"
  }

  test("408 REQUEST_TIMEOUT error response") {
    val response = classUnderTest.onClientError(dummyRequest, 408, "Request timeout")
    status(response) mustEqual 408
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "REQUEST_TIMEOUT"
    result.message mustEqual "Request timeout"
  }

  test("413 PAYLOAD_TOO_LARGE error response") {
    val response = classUnderTest.onClientError(dummyRequest, 413, "Payload too large")
    status(response) mustEqual 413
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "PAYLOAD_TOO_LARGE"
    result.message mustEqual "Payload too large"
  }

  test("415 UNSUPPORTED_MEDIA_TYPE error response") {
    val response = classUnderTest.onClientError(dummyRequest, 415, "Unsupported media type")
    status(response) mustEqual 415
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "UNSUPPORTED_MEDIA_TYPE"
    result.message mustEqual "Unsupported media type"
  }

  test("Unhandled client error status code response") {
    val response = classUnderTest.onClientError(dummyRequest, 418, "I'm a teapot")
    status(response) mustEqual 418
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "418"
    result.message mustEqual "I'm a teapot"
  }

  test("Catch-all error response") {
    val response = classUnderTest.onServerError(dummyRequest, new RuntimeException("Generic Error"))
    status(response) mustEqual 500
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "500"
    result.message mustEqual "Internal Server Error"
  }

  test("EmptyRequestBody error response") {
    val response = classUnderTest.onServerError(dummyRequest, EmptyRequestBody)
    status(response) mustEqual 400
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "EMPTY_REQUEST_BODY"
    result.message mustEqual "Empty request body"
  }

  test("InvalidDateFormat error response") {
    val response = classUnderTest.onServerError(dummyRequest, InvalidDateFormat)
    status(response) mustEqual 400
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "INVALID_DATE_FORMAT"
    result.message mustEqual "Invalid date format"
  }

  test("InvalidDateRange error response") {
    val response = classUnderTest.onServerError(dummyRequest, InvalidDateRange)
    status(response) mustEqual 400
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "INVALID_DATE_RANGE"
    result.message mustEqual "Invalid date range"
  }

  test("MissingHeader error response") {
    val response = classUnderTest.onServerError(dummyRequest, MissingHeader("Missing Header"))
    status(response) mustEqual 400
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "MISSING_HEADER"
    result.message mustEqual "Missing Header"
  }

  test("InvalidJson error response") {
    val response = classUnderTest.onServerError(dummyRequest, InvalidJson)
    status(response) mustEqual 400
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "INVALID_JSON"
    result.message mustEqual "Invalid JSON payload"
  }

  test("IncorrectHeaderValue error response") {
    val response = classUnderTest.onServerError(dummyRequest, IncorrectHeaderValue)
    status(response) mustEqual 400
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "INCORRECT_HEADER_VALUE"
    result.message mustEqual "X-Pillar2-Id Header value does not match the bearer token"
  }

  test("MissingCredentials error response") {
    val response = classUnderTest.onServerError(dummyRequest, MissingCredentials)
    status(response) mustEqual 401
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "MISSING_CREDENTIALS"
    result.message mustEqual "Authentication information is not provided"
  }

  test("InvalidCredentials error response") {
    val response = classUnderTest.onServerError(dummyRequest, InvalidCredentials)
    status(response) mustEqual 401
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "INVALID_CREDENTIALS"
    result.message mustEqual "Invalid Authentication information provided"
  }

  test("ForbiddenError error response") {
    val response = classUnderTest.onServerError(dummyRequest, ForbiddenError)
    status(response) mustEqual 403
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "FORBIDDEN"
    result.message mustEqual "Access to the requested resource is forbidden"
  }

  test("InvalidEnrolment error response") {
    val response = classUnderTest.onServerError(dummyRequest, InvalidEnrolment)
    status(response) mustEqual 403
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "INVALID_ENROLMENT"
    result.message mustEqual "Invalid Pillar 2 enrolment"
  }

  test("NoSubscriptionData error response") {
    val response = classUnderTest.onServerError(dummyRequest, NoSubscriptionData("XTC01234123412"))
    status(response) mustEqual 500
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "004"
    result.message mustEqual "No Pillar2 subscription found for XTC01234123412"
  }

  test("DownstreamValidationError error response") {
    val response = classUnderTest.onServerError(dummyRequest, DownstreamValidationError("093", "Invalid Return"))
    status(response) mustEqual 422
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "093"
    result.message mustEqual "Invalid Return"
  }

  test("TestEndpointDisabled response") {
    val response = classUnderTest.onServerError(dummyRequest, TestEndpointDisabled)
    status(response) mustEqual 403
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "TEST_ENDPOINT_DISABLED"
    result.message mustEqual "Test endpoints are not available in this environment"
  }

  test("DatabaseError response") {
    val response = classUnderTest.onServerError(dummyRequest, DatabaseError("write"))
    status(response) mustEqual 500
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "500"
    result.message mustEqual "Failed to write organisation due to database error"
  }

  test("UnexpectedResponse error response") {
    val response = classUnderTest.onServerError(dummyRequest, UnexpectedResponse)
    status(response) mustEqual 500
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "500"
    result.message mustEqual "Internal Server Error"
  }
}
