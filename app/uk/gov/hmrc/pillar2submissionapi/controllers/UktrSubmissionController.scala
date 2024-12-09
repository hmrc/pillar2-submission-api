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
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.pillar2submissionapi.controllers.actions.IdentifierAction
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.UktrSubmission
import uk.gov.hmrc.pillar2submissionapi.services.SubmitUktrService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UktrSubmissionController @Inject() (cc: ControllerComponents,
                                          val identify: IdentifierAction,
                                          val submitUktrService: SubmitUktrService)
                                         (implicit val ec: ExecutionContext)
extends BackendController(cc) {

  def submitUktr: Action[AnyContent] = identify.async { implicit request =>
    request.body.asJson match {
      case Some(value) =>
        value.validate[UktrSubmission] match {
          case JsSuccess(value, _) =>
            submitUktrService
              .submitUktr(value)
              .map(response => Created(Json.toJson(response)))
          case JsError(_) =>
            Future.successful(BadRequest(""))
        }
      case None =>
        Future.successful(BadRequest(""))
    }
  }
}
