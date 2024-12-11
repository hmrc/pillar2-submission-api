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

import play.api.http.HttpErrorHandler
import play.api.libs.json.Json
import play.api.mvc.{RequestHeader, Result, Results}
import uk.gov.hmrc.pillar2submissionapi.controllers.error._

import scala.concurrent.Future

class Pillar2ErrorHandler extends HttpErrorHandler {

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] =
    Future.successful(Results.BadRequest(Json.toJson(Pillar2ErrorResponse(statusCode.toString, message))))

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] =
    exception match {
      case e if e.isInstanceOf[Pillar2Error] =>
        val pillar2Error: Pillar2Error = e.asInstanceOf[Pillar2Error]
        pillar2Error match {
          case e @ InvalidJson =>
            Future.successful(Results.BadRequest(Pillar2ErrorResponse(e.code, "Invalid JSON Payload")))
          case e @ EmptyRequestBody             => Future.successful(Results.BadRequest(Pillar2ErrorResponse(e.code, "Empty body in request")))
          case e @ AuthenticationError(message) => Future.successful(Results.Unauthorized(Pillar2ErrorResponse(e.code, message)))
          case e @ NoSubscriptionData(_)        => Future.successful(Results.InternalServerError(Pillar2ErrorResponse(e.code, e.message)))
        }
      case _ =>
        Future.successful(Results.InternalServerError(Pillar2ErrorResponse("500", "Internal Server Error")))
    }
}
