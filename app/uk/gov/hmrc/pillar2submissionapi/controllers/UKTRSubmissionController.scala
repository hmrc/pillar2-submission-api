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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.controllers.actions.{IdentifierAction, SubscriptionDataRetrievalAction}
import uk.gov.hmrc.pillar2submissionapi.controllers.error.{EmptyRequestBody, InvalidJson}
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.UKTRSubmission
import uk.gov.hmrc.pillar2submissionapi.services.SubmitUKTRService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UKTRSubmissionController @Inject() (
  cc:                       ControllerComponents,
  identify:                 IdentifierAction,
  verifySubscriptionExists: SubscriptionDataRetrievalAction,
  submitUKTRService:        SubmitUKTRService
)(implicit ec:              ExecutionContext)
    extends BackendController(cc) {

  def submitUKTR: Action[AnyContent] = (identify andThen verifySubscriptionExists).async { request =>
    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter
        .fromRequest(request)
        .withExtraHeaders("X-Pillar2-Id" -> request.clientPillar2Id)
    request.body.asJson match {
      case Some(request) =>
        request.validate[UKTRSubmission] match {
          case JsSuccess(value, _) =>
            submitUKTRService
              .submitUktr(value)
              .map(response => Created(Json.toJson(response)))
          case JsError(_) => Future.failed(InvalidJson)
        }
      case None => Future.failed(EmptyRequestBody)
    }
  }
}