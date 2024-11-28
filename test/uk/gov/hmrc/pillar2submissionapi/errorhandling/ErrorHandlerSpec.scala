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

package uk.gov.hmrc.pillar2submissionapi.errorhandling

import play.api.http.Status
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, contentType, status}
import uk.gov.hmrc.pillar2submissionapi.helpers.BaseSpec

import scala.concurrent.Future

class ErrorHandlerSpec extends BaseSpec {

  val errorHandler = new ErrorHandler

  "ErrorHandler" - {

    "onClientError" - {
      "should return a JSON response for client errors" in {
        val request    = FakeRequest("GET", "/some-endpoint")
        val statusCode = Status.BAD_REQUEST
        val message    = "Invalid input"

        val result: Future[Result] = errorHandler.onClientError(request, statusCode, message)

        status(result)      shouldBe statusCode:               Unit
        contentType(result) shouldBe Some("application/json"): Unit
        val jsonResponse = contentAsJson(result)
        (jsonResponse \ "status").as[Int]        shouldBe statusCode:                         Unit
        (jsonResponse \ "message").as[String]    shouldBe message:                            Unit
        (jsonResponse \ "details").as[Seq[String]] should contain("A client error occurred"): Unit
      }
    }

    "onServerError" - {
      "should return a JSON response for server errors" in {
        val request   = FakeRequest("GET", "/some-endpoint")
        val exception = new RuntimeException("Unexpected error")

        val result: Future[Result] = errorHandler.onServerError(request, exception)

        status(result)      shouldBe Status.INTERNAL_SERVER_ERROR: Unit
        contentType(result) shouldBe Some("application/json"):     Unit
        val jsonResponse = contentAsJson(result)
        (jsonResponse \ "status").as[Int]        shouldBe Status.INTERNAL_SERVER_ERROR:  Unit
        (jsonResponse \ "message").as[String]    shouldBe "Internal Server Error":       Unit
        (jsonResponse \ "details").as[Seq[String]] should contain(exception.getMessage): Unit
      }
    }
  }
}
