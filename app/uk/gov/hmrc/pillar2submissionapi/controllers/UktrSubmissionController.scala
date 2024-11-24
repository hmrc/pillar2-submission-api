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

import cats.data.Validated
import cats.implicits.toFoldableOps
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.{UktrSubmission, UktrSubmissionData, UktrSubmissionNilReturn}
import uk.gov.hmrc.pillar2submissionapi.validation._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}

@Singleton
class UktrSubmissionController @Inject() (cc: ControllerComponents) extends BackendController(cc) {

  def submitUktr: Action[AnyContent] = Action { request =>
    request.body.asJson match {
      case Some(json) =>
        json.validate[UktrSubmission] match {
          case JsSuccess(submission, _) =>
            submission match {
              case data: UktrSubmissionData =>
                LiabilityDataValidator.validate(data.liabilities) match {
                  case Validated.Valid(_) =>
                    Created(Json.obj("status" -> "Created"))
                  case Validated.Invalid(errors) =>
                    BadRequest(
                      Json.obj(
                        "message" -> "Invalid JSON format",
                        "details" -> errors.toList.map(e => s"${e.field}: ${e.error}")
                      )
                    )
                }
              case nilReturn: UktrSubmissionNilReturn =>
                LiabilityNilReturnValidator.validate(nilReturn.liabilities) match {
                  case Validated.Valid(_) =>
                    Created(Json.obj("status" -> "Created"))
                  case Validated.Invalid(errors) =>
                    BadRequest(
                      Json.obj(
                        "message" -> "Invalid JSON format",
                        "details" -> errors.toList.map(e => s"${e.field}: ${e.error}")
                      )
                    )
                }
              case _ =>
                BadRequest(Json.obj("message" -> "Unknown submission type"))
            }
          case JsError(errors) =>
            val unknownTypeError = errors.exists { case (_, validationErrors) =>
              validationErrors.exists(_.message == "Unknown submission type")
            }

            if (unknownTypeError) {
              BadRequest(Json.obj("message" -> "Unknown submission type"))
            } else {
              val errorDetails = errors.map { case (path, validationErrors) =>
                s"Path: $path, Errors: ${validationErrors.map(_.message).mkString(", ")}"
              }
              BadRequest(Json.obj("message" -> "Invalid JSON format", "details" -> errorDetails))
            }
        }
      case None =>
        BadRequest(Json.obj("message" -> "Invalid JSON format"))
    }
  }
}
