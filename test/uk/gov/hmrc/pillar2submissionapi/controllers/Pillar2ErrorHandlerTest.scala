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

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}
import uk.gov.hmrc.pillar2submissionapi.controllers.error._

class Pillar2ErrorHandlerTest extends AnyFunSuite {

  val classUnderTest = new Pillar2ErrorHandler
  val dummyRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
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

  test("InvalidJson error response") {
    val response = classUnderTest.onServerError(dummyRequest, InvalidJson)
    status(response) mustEqual 400
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "001"
    result.message mustEqual "Invalid JSON Payload"
  }

  test("AuthenticationError error response") {
    val response = classUnderTest.onServerError(dummyRequest, AuthenticationError("Authentication Error"))
    status(response) mustEqual 401
    val result = contentAsJson(response).as[Pillar2ErrorResponse]
    result.code mustEqual "003"
    result.message mustEqual "Authentication Error"
  }
}
