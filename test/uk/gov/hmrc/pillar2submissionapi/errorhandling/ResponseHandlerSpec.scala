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

import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.pillar2submissionapi.helpers.BaseSpec

import scala.concurrent.Future

class ResponseHandlerSpec extends BaseSpec {

  "ResponseHandler" - {

    "badRequest" - {

      val responseHandler = new ResponseHandler

      "should return BadRequest with default error message when no details are provided" in {

        val result: Future[Result] = Future.successful(responseHandler.badRequest("Invalid JSON format", None))

        status(result) shouldBe BAD_REQUEST: Unit
        val jsonResponse = contentAsJson(result)
        (jsonResponse \ "message").as[String]    shouldBe "Invalid JSON format":                   Unit
        (jsonResponse \ "details").as[Seq[String]] should contain("An unexpected error occurred"): Unit
      }

      "should return BadRequest with provided details" in {

        val details = Seq("Detail 1", "Detail 2")

        val result: Future[Result] = Future.successful(responseHandler.badRequest("Invalid JSON format", Some(details)))

        status(result) shouldBe BAD_REQUEST: Unit
        val jsonResponse = contentAsJson(result)
        (jsonResponse \ "message").as[String]    shouldBe "Invalid JSON format":         Unit
        (jsonResponse \ "details").as[Seq[String]] should contain allElementsOf details: Unit
      }
    }

    "created" - {

      "should return Created with the correct JSON response" in {

        val responseHandler = new ResponseHandler

        val result: Future[Result] = Future.successful(responseHandler.created())

        status(result) shouldBe CREATED: Unit
        val jsonResponse = contentAsJson(result)
        (jsonResponse \ "status").as[String] shouldBe "Created"
      }
    }
  }
}
