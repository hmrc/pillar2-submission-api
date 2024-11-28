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

import play.api.http.{HttpErrorHandler, Status}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.Future

class ErrorHandler extends HttpErrorHandler {

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    val errorResponse = Json.obj(
      "status"  -> statusCode,
      "message" -> message,
      "details" -> Seq("A client error occurred")
    )

    Future.successful(
      Results
        .Status(statusCode)(Json.toJson(errorResponse))
        .withHeaders("Content-Type" -> "application/json")
    )
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    val errorResponse = Json.obj(
      "status"  -> Status.INTERNAL_SERVER_ERROR,
      "message" -> "Internal Server Error",
      "details" -> Seq(exception.getMessage)
    )

    Future.successful(
      Results
        .Status(Status.INTERNAL_SERVER_ERROR)(Json.toJson(errorResponse))
        .withHeaders("Content-Type" -> "application/json")
    )
  }
}
