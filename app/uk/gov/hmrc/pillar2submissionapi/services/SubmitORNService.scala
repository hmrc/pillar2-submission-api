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

import com.google.inject.{Inject, Singleton}
import play.api.Logging
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.connectors.SubmitORNConnector
import uk.gov.hmrc.pillar2submissionapi.controllers.error.{ORNValidationError, UnexpectedResponse}
import uk.gov.hmrc.pillar2submissionapi.models.overseasreturnnotification.{ORNErrorResponse, ORNSubmission, ORNSuccessResponse}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmitORNService @Inject() (submitORNConnector: SubmitORNConnector)(implicit val ec: ExecutionContext) extends Logging {

  def submitORN(request: ORNSubmission)(implicit hc: HeaderCarrier): Future[ORNSuccessResponse] =
    submitORNConnector.submitORN(request).map(convertToResult)

  private def convertToResult(response: HttpResponse): ORNSuccessResponse =
    response.status match {
      case 201 =>
        response.json.validate[ORNSuccessResponse] match {
          case JsSuccess(success, _) => success
          case JsError(_) =>
            logger.error("Failed to parse success response")
            throw UnexpectedResponse
        }
      case 422 =>
        response.json.validate[ORNErrorResponse] match {
          case JsSuccess(response, _) => throw ORNValidationError(response.code, response.message)
          case JsError(_) =>
            logger.error("Failed to parse unprocessible entity response")
            throw UnexpectedResponse
        }
      case status =>
        logger.error(s"Error calling pillar2 backend. Got response: $status")
        throw UnexpectedResponse
    }
}
