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

import play.api.Logging
import play.api.http.{DefaultHttpErrorHandler, MimeTypes}
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}

import javax.inject.Singleton
import scala.concurrent.Future

@Singleton
class ErrorHandler extends DefaultHttpErrorHandler with Logging {

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    val errorResponse = Json.obj(
      "status"  -> statusCode,
      "message" -> message
    )
    Future.successful(Status(statusCode)(errorResponse).as(MimeTypes.JSON))
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    val errorResponse = Json.obj(
      "status"  -> 500,
      "message" -> "Internal Server Error",
      "details" -> exception.getMessage
    )
    Future.successful(InternalServerError(errorResponse).as(MimeTypes.JSON))
  }
}
//
//package uk.gov.hmrc.pillar2submissionapi.error
//
//import play.api.Logging
//import play.api.libs.json.Json
//import play.api.mvc.Results._
//import play.api.mvc.{RequestHeader, Result}
//import play.api.http.HttpErrorHandler
//
//import javax.inject.Singleton
//import scala.concurrent.Future
//
//@Singleton
//class ErrorHandler extends HttpErrorHandler with Logging {
//  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
//    logger.error(s"Client error occurred: $message for ${request.uri}")
//    Future.successful(
//      Status(statusCode)(
//        Json.obj("message" -> s"Client error: $message")
//      )
//    )
//  }
//
//  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
//    logger.error(s"Server error occurred for ${request.uri}", exception)
//    Future.successful(
//      InternalServerError(
//        Json.obj("message" -> "Internal server error")
//      )
//    )
//  }
//}
