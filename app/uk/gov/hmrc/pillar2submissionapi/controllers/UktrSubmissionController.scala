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

import play.api.libs.json.{JsError, JsSuccess}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.pillar2submissionapi.controllers.actions.{IdentifierAction, SubscriptionDataRetrievalAction}
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.UktrSubmission
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}

@Singleton
class UktrSubmissionController @Inject() (
  cc:       ControllerComponents,
  identify: IdentifierAction,
  getData:  SubscriptionDataRetrievalAction
) extends BackendController(cc) {

  def submitUktr: Action[AnyContent] = (identify andThen getData) { request =>
    request.subscriptionData match {
      case Right(_) =>
        request.body.asJson match {
          case Some(json) =>
            json.validate[UktrSubmission] match {
              case JsSuccess(_, _) => Created
              case JsError(_)      => BadRequest("Bad request")
            }
          case None => BadRequest("No request body")
        }
      case Left(_) => BadRequest("No subscription data")
    }
  }
}
