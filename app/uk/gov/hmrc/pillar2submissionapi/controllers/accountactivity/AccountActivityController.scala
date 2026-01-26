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

package uk.gov.hmrc.pillar2submissionapi.controllers.accountactivity

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.controllers.actions.{IdentifierAction, Pillar2IdHeaderExistsAction}
import uk.gov.hmrc.pillar2submissionapi.controllers.error.{InvalidDateFormat, InvalidDateRange}
import uk.gov.hmrc.pillar2submissionapi.services.AccountActivityService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AccountActivityController @Inject() (
  accountActivityService: AccountActivityService,
  identify:               IdentifierAction,
  pillar2IdAction:        Pillar2IdHeaderExistsAction,
  cc:                     ControllerComponents
)(using ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def retrieveAccountActivity(fromDate: String, toDate: String): Action[AnyContent] =
    (pillar2IdAction andThen identify).async { request =>
      given HeaderCarrier = hc(request).withExtraHeaders("X-Pillar2-Id" -> request.clientPillar2Id)

      Try(LocalDate.parse(fromDate))
        .flatMap(from => Try(LocalDate.parse(toDate)).map(to => (from, to)))
        .fold(
          _ => Future.failed(InvalidDateFormat),
          (from, to) =>
            if from.isAfter(to) then Future.failed(InvalidDateRange)
            else {
              accountActivityService
                .retrieveAccountActivity(fromDate = from, toDate = to)
                .value
                .flatMap {
                  case Left(error) =>
                    logger.warn(s"Encountered $error while fetching account activity.")
                    Future.failed(error)
                  case Right(accountActivity) => Future.successful(Ok(Json.toJson(accountActivity)))
                }
            }
        )
    }
}
