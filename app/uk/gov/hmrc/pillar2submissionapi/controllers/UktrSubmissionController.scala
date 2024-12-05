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

import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.connectors.Pillar2Connector
import uk.gov.hmrc.pillar2submissionapi.controllers.actions.IdentifierAction
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.UktrSubmission
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.responses.ApiResponse.internalServerError
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.responses.{SubmitUktrErrorResponse, SubmitUktrSuccessResponse}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class UktrSubmissionController @Inject() (cc: ControllerComponents, identify: IdentifierAction, pillar2Connector: Pillar2Connector)(implicit
  val hc:                                     HeaderCarrier,
  ec:                                         ExecutionContext
) extends BackendController(cc) {

  def submitUktr: Action[AnyContent] = identify.async { implicit request =>
    request.body.asJson match {
      case Some(value) =>
        value.validate[UktrSubmission] match {
          case JsSuccess(value, _) =>
            pillar2Connector
              .submitUktr(value)
              .map(convertToResult)
          case JsError(_) =>
            Future.successful(BadRequest(""))
        }
      case None =>
        Future.successful(BadRequest(""))
    }
  }

  private def convertToResult(response: HttpResponse): Result =
    response.status match {
      case 201 =>
        response.json.validate[SubmitUktrSuccessResponse] match {
          case JsSuccess(success, _) => Created(Json.toJson(success))
          case JsError(errors) =>
            InternalServerError(
              Json.toJson(SubmitUktrErrorResponse("500", s"Failed to parse success response: $errors"))
            )
        }
      case 422 =>
        response.json.validate[SubmitUktrErrorResponse] match {
          case JsSuccess(response, _) =>
            UnprocessableEntity(Json.toJson(SubmitUktrErrorResponse(response.code, response.message)))
          case JsError(_) =>
            InternalServerError(Json.toJson(internalServerError))
        }
      case _ =>
        InternalServerError(Json.toJson(internalServerError))
    }
}
