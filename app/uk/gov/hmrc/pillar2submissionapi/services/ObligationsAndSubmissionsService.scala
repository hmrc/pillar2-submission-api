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

package uk.gov.hmrc.pillar2submissionapi.services

import play.api.Logging
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.connectors.ObligationAndSubmissionsConnector
import uk.gov.hmrc.pillar2submissionapi.controllers.error.{DownstreamValidationError, UnexpectedResponse}
import uk.gov.hmrc.pillar2submissionapi.models.obligationsandsubmissions.{ObligationsAndSubmissionsErrorResponse, ObligationsAndSubmissionsSuccessResponse}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ObligationsAndSubmissionsService @Inject() (
  obligationAndSubmissionsConnector: ObligationAndSubmissionsConnector
)(implicit ec:                       ExecutionContext)
    extends Logging {

  def handleData(fromDate: LocalDate, toDate: LocalDate)(implicit hc: HeaderCarrier): Future[ObligationsAndSubmissionsSuccessResponse] =
    obligationAndSubmissionsConnector.getData(fromDate, toDate).map(convertToResult)

  private def convertToResult(response: HttpResponse): ObligationsAndSubmissionsSuccessResponse =
    response.status match {
      case 200 =>
        response.json.validate[ObligationsAndSubmissionsSuccessResponse] match {
          case JsSuccess(success, _) => success
          case JsError(_) =>
            logger.error("Failed to parse success response")
            throw UnexpectedResponse
        }
      case 422 =>
        response.json.validate[ObligationsAndSubmissionsErrorResponse] match {
          case JsSuccess(response, _) => throw DownstreamValidationError(response.code, response.message)
          case JsError(_) =>
            logger.error("Failed to parse unprocessible entity response")
            throw UnexpectedResponse
        }
      case status =>
        logger.error(s"Error calling pillar2 backend. Got response: $status")
        throw UnexpectedResponse
    }
}
