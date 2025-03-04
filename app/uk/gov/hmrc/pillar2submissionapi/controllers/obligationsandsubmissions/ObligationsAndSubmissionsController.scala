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

package uk.gov.hmrc.pillar2submissionapi.controllers.obligationsandsubmissions

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.controllers.actions.IdentifierAction
import uk.gov.hmrc.pillar2submissionapi.controllers.error.InvalidRequest
import uk.gov.hmrc.pillar2submissionapi.models.obligationsandsubmissions.ObligationsAndSubmissions
import uk.gov.hmrc.pillar2submissionapi.services.ObligationsAndSubmissionsService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class ObligationsAndSubmissionsController @Inject() (
  cc:                              ControllerComponents,
  identify:                        IdentifierAction,
  obligationAndSubmissionsService: ObligationsAndSubmissionsService
)(implicit ec:                     ExecutionContext)
    extends BackendController(cc) {

  def retrieveData(fromDate: String, toDate: String): Action[AnyContent] = identify.async { request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request).withExtraHeaders("X-Pillar2-Id" -> request.clientPillar2Id)
    Try {
      val accountingPeriod = ObligationsAndSubmissions(fromDate = LocalDate.parse(fromDate), toDate = LocalDate.parse(toDate))

      if (accountingPeriod.validDateRange) {
        obligationAndSubmissionsService
          .handleData(accountingPeriod.fromDate, accountingPeriod.toDate)
          .map(response => Ok(Json.toJson(response)))
      } else { Future.failed(InvalidRequest) }
    }.getOrElse(Future.failed(InvalidRequest))
  }
}
