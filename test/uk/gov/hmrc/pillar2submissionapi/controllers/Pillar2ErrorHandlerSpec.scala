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
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
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
      status(result) mustEqual 400
      val response = contentAsJson(result).as[Pillar2ErrorResponse]
      response.message mustEqual message
      response.code mustEqual statusCode.toString
    }
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
    result.code mustEqual "002"
    result.message mustEqual "Empty body in request"
  }

  test("InvalidRequest error response") {
    val response = classUnderTest.onServerError(dummyRequest, InvalidRequest)
    status(response) mustEqual 400
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "000"
    result.message mustEqual "Invalid Request"
  }

  test("MissingHeader error response") {
    val response = classUnderTest.onServerError(dummyRequest, MissingHeader("Missing Header"))
    status(response) mustEqual 400
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "005"
  }

  test("InvalidJson error response") {
    val response = classUnderTest.onServerError(dummyRequest, InvalidJson)
    status(response) mustEqual 400
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "001"
    result.message mustEqual "Invalid JSON Payload"
  }

  test("AuthenticationError error response") {
    val response = classUnderTest.onServerError(dummyRequest, AuthenticationError)
    status(response) mustEqual 401
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "003"
    result.message mustEqual "Not authorized"
  }

  test("ForbiddenError error response") {
    val response = classUnderTest.onServerError(dummyRequest, ForbiddenError)
    status(response) mustEqual 403
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "006"
    result.message mustEqual "Forbidden"
  }

  test("NoSubscriptionData error response") {
    val response = classUnderTest.onServerError(dummyRequest, NoSubscriptionData("XTC01234123412"))
    status(response) mustEqual 500
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "004"
    result.message mustEqual "No Pillar2 subscription found for XTC01234123412"
  }

  test("UktrValidationError error response") {
    val response = classUnderTest.onServerError(dummyRequest, UktrValidationError("093", "Invalid Return"))
    status(response) mustEqual 422
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "093"
    result.message mustEqual "Invalid Return"
  }

  test("BTNValidationError error response") {
    val response = classUnderTest.onServerError(dummyRequest, BTNValidationError("093", "Invalid Return"))
    status(response) mustEqual 422
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "093"
    result.message mustEqual "Invalid Return"
  }

  test("ObligationsAndSubmissionsValidationError response") {
    val response = classUnderTest.onServerError(dummyRequest, ObligationsAndSubmissionsValidationError("code", "message"))
    status(response) mustEqual 422
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "code"
    result.message mustEqual "message"
  }

  test("TestEndpointDisabled response") {
    val response = classUnderTest.onServerError(dummyRequest, TestEndpointDisabled())
    status(response) mustEqual 403
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "403"
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
