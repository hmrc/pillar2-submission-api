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

package uk.gov.hmrc.pillar2submissionapi.controllers.submission

import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.controllers.actions.{IdentifierAction, Pillar2IdHeaderAction, SubscriptionDataRetrievalAction}
import uk.gov.hmrc.pillar2submissionapi.controllers.error._
import uk.gov.hmrc.pillar2submissionapi.models.obligationsandsubmissions.ObligationsAndSubmissions
import uk.gov.hmrc.pillar2submissionapi.models.overseasreturnnotification.ORNSubmission
import uk.gov.hmrc.pillar2submissionapi.services.OverseasReturnNotificationService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class OverseasReturnNotificationController @Inject() (
  cc:              ControllerComponents,
  identify:        IdentifierAction,
  pillar2Action:   Pillar2IdHeaderAction,
  getSubscription: SubscriptionDataRetrievalAction,
  ornService:      OverseasReturnNotificationService
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def submitORN: Action[AnyContent] = (pillar2Action andThen identify andThen getSubscription).async { request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request).withExtraHeaders("X-Pillar2-Id" -> request.clientPillar2Id)
    request.body.asJson match {
      case Some(request) =>
        request.validate[ORNSubmission] match {
          case JsSuccess(value, _) =>
            ornService
              .submitORN(value)
              .map(response => Created(Json.toJson(response)))
          case JsError(_) => Future.failed(InvalidJson)
        }
      case None => Future.failed(EmptyRequestBody)
    }
  }

  def amendORN: Action[AnyContent] = (pillar2Action andThen identify andThen getSubscription).async { request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request).withExtraHeaders("X-Pillar2-Id" -> request.clientPillar2Id)
    request.body.asJson match {
      case Some(request) =>
        request.validate[ORNSubmission] match {
          case JsSuccess(value, _) =>
            ornService
              .amendORN(value)
              .map(response => Ok(Json.toJson(response)))
          case JsError(_) => Future.failed(InvalidJson)
        }
      case None => Future.failed(EmptyRequestBody)
    }
  }

  def retrieveORN(accountingPeriodFrom: String, accountingPeriodTo: String): Action[AnyContent] =
    (pillar2Action andThen identify andThen getSubscription).async { implicit request =>
      val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request).withExtraHeaders("X-Pillar2-Id" -> request.clientPillar2Id)

      Try {
        val accountingPeriod =
          ObligationsAndSubmissions(fromDate = LocalDate.parse(accountingPeriodFrom), toDate = LocalDate.parse(accountingPeriodTo))
        if (accountingPeriod.validDateRange) {
          ornService
            .retrieveORN(accountingPeriodFrom, accountingPeriodTo)(using hc)
            .map(response => Ok(Json.toJson(response)))
        } else { Future.failed(InvalidDateRange) }
      }.getOrElse(Future.failed(InvalidDateFormat))
    }
}
