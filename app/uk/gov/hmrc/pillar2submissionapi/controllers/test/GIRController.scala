/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.pillar2submissionapi.controllers.test

import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.config.AppConfig
import uk.gov.hmrc.pillar2submissionapi.controllers.actions.{IdentifierAction, Pillar2IdHeaderExistsAction}
import uk.gov.hmrc.pillar2submissionapi.controllers.error.{EmptyRequestBody, InvalidJson, TestEndpointDisabled}
import uk.gov.hmrc.pillar2submissionapi.models.globeinformationreturn.GIRSubmission
import uk.gov.hmrc.pillar2submissionapi.services.GIRService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GIRController @Inject() (
  cc:              ControllerComponents,
  identify:        IdentifierAction,
  pillar2IdAction: Pillar2IdHeaderExistsAction,
  girService:      GIRService,
  config:          AppConfig
)(using ec: ExecutionContext)
    extends BackendController(cc) {

  private def checkTestEndpointsEnabled[A](block: => Future[A]): Future[A] =
    if (config.testOrganisationEnabled) block else Future.failed(TestEndpointDisabled)

  def createGIR: Action[AnyContent] = (pillar2IdAction andThen identify).async { request =>
    given hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request).withExtraHeaders("X-Pillar2-Id" -> request.clientPillar2Id)
    checkTestEndpointsEnabled {
      request.body.asJson match {
        case Some(json) =>
          json.validate[GIRSubmission] match {
            case JsSuccess(value, _) =>
              girService
                .createGIR(value)
                .map(response => Created(Json.toJson(response)))
            case JsError(_) => Future.failed(InvalidJson)
          }
        case None => Future.failed(EmptyRequestBody)
      }
    }
  }
}
