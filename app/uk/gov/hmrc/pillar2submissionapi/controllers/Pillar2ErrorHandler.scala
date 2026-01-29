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

import play.api.Logging
import play.api.http.HttpErrorHandler
import play.api.libs.json.Json
import play.api.mvc.Results.Status
import play.api.mvc.{RequestHeader, Result, Results}
import uk.gov.hmrc.pillar2submissionapi.controllers.error._
import uk.gov.hmrc.pillar2submissionapi.models.response.Pillar2ErrorResponse

import scala.concurrent.Future

class Pillar2ErrorHandler extends HttpErrorHandler with Logging {

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    val errorResponse = statusCode match {
      case 400 => Pillar2ErrorResponse("BAD_REQUEST", "Invalid request")
      case 408 => Pillar2ErrorResponse("REQUEST_TIMEOUT", message)
      case 413 => Pillar2ErrorResponse("PAYLOAD_TOO_LARGE", message)
      case 415 => Pillar2ErrorResponse("UNSUPPORTED_MEDIA_TYPE", message)
      case _   => Pillar2ErrorResponse(statusCode.toString, message)
    }
    Future.successful(Status(statusCode)(Json.toJson(errorResponse)))
  }
  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] =
    exception match {
      case e: Pillar2Error =>
        val ret = e match {
          case InvalidDateRange | InvalidDateFormat | InvalidJson | EmptyRequestBody | MissingHeader(_) | IncorrectHeaderValue =>
            Results.BadRequest(Pillar2ErrorResponse(e.code, e.message))
          case MissingCredentials | InvalidCredentials                       => Results.Unauthorized(Pillar2ErrorResponse(e.code, e.message))
          case ForbiddenError | InvalidEnrolment | TestEndpointDisabled      => Results.Forbidden(Pillar2ErrorResponse(e.code, e.message))
          case OrganisationNotFound(_) | ORNNotFoundException                => Results.NotFound(Pillar2ErrorResponse(e.code, e.message))
          case OrganisationAlreadyExists(_)                                  => Results.Conflict(Pillar2ErrorResponse(e.code, e.message))
          case DownstreamValidationError(_, _)                               => Results.UnprocessableEntity(Pillar2ErrorResponse(e.code, e.message))
          case AccountActivityNotAvailable                                   => Results.NotImplemented(Pillar2ErrorResponse(e.code, e.message))
          case DatabaseError(_) | UnexpectedResponse | NoSubscriptionData(_) =>
            Results.InternalServerError(Pillar2ErrorResponse(e.code, e.message))
        }
        logger.warn(s"Caught Pillar2Error. Returning ${ret.header.status} statuscode", exception)
        Future.successful(ret)
      case _ =>
        logger.warn("Unhandled exception. Returning 500 statuscode", exception)
        Future.successful(Results.InternalServerError(Pillar2ErrorResponse("500", "Internal Server Error")))
    }
}
