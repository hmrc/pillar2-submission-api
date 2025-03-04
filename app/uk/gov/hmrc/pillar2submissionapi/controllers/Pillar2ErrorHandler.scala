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
import play.api.mvc.{RequestHeader, Result, Results}
import uk.gov.hmrc.pillar2submissionapi.controllers.error._
import uk.gov.hmrc.pillar2submissionapi.models.response.Pillar2ErrorResponse

import scala.concurrent.Future

class Pillar2ErrorHandler extends HttpErrorHandler with Logging {

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] =
    Future.successful(Results.BadRequest(Json.toJson(Pillar2ErrorResponse(statusCode.toString, message))))

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] =
    exception match {
      case e if e.isInstanceOf[Pillar2Error] =>
        val pillar2Error: Pillar2Error = e.asInstanceOf[Pillar2Error]
        val ret = pillar2Error match {
          case e @ InvalidRequest                                 => Results.BadRequest(Pillar2ErrorResponse(e.code, e.message))
          case e @ InvalidJson                                    => Results.BadRequest(Pillar2ErrorResponse(e.code, e.message))
          case e @ EmptyRequestBody                               => Results.BadRequest(Pillar2ErrorResponse(e.code, e.message))
          case e @ MissingHeader(_)                               => Results.BadRequest(Pillar2ErrorResponse(e.code, e.message))
          case e @ AuthenticationError                            => Results.Unauthorized(Pillar2ErrorResponse(e.code, e.message))
          case e @ ForbiddenError                                 => Results.Forbidden(Pillar2ErrorResponse(e.code, e.message))
          case e @ NoSubscriptionData(_)                          => Results.InternalServerError(Pillar2ErrorResponse(e.code, e.message))
          case e @ UktrValidationError(_, _)                      => Results.UnprocessableEntity(Pillar2ErrorResponse(e.code, e.message))
          case e @ BTNValidationError(_, _)                       => Results.UnprocessableEntity(Pillar2ErrorResponse(e.code, e.message))
          case e @ ObligationsAndSubmissionsValidationError(_, _) => Results.UnprocessableEntity(Pillar2ErrorResponse(e.code, e.message))
          case e @ OrganisationAlreadyExists(_)                   => Results.Conflict(Pillar2ErrorResponse(e.code, e.message))
          case e @ OrganisationNotFound(_)                        => Results.NotFound(Pillar2ErrorResponse(e.code, e.message))
          case e @ DatabaseError(_)                               => Results.InternalServerError(Pillar2ErrorResponse(e.code, e.message))
          case e @ UnexpectedResponse                             => Results.InternalServerError(Pillar2ErrorResponse(e.code, e.message))
          case e @ TestEndpointDisabled()                         => Results.Forbidden(Pillar2ErrorResponse(e.code, e.message))
        }
        logger.warn(s"Caught Pillar2Error. Returning ${ret.header.status} statuscode", exception)
        Future.successful(ret)
      case _ =>
        logger.warn("Unhandled exception. Returning 500 statuscode", exception)
        Future.successful(Results.InternalServerError(Pillar2ErrorResponse("500", "Internal Server Error")))
    }
}
