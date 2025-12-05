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
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.connectors.OverseasReturnNotificationConnector
import uk.gov.hmrc.pillar2submissionapi.controllers.error.{DownstreamValidationError, ORNNotFoundException, UnexpectedResponse}
import uk.gov.hmrc.pillar2submissionapi.models.overseasreturnnotification.*

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OverseasReturnNotificationService @Inject() (connector: OverseasReturnNotificationConnector)(using ec: ExecutionContext) extends Logging {

  def submitORN(request: ORNSubmission)(using hc: HeaderCarrier): Future[ORNSuccessResponse] =
    connector.submitORN(request).map(convertToSubmitResult)

  def amendORN(request: ORNSubmission)(using hc: HeaderCarrier): Future[ORNSuccessResponse] =
    connector.amendORN(request).map(convertToSubmitResult)

  def retrieveORN(accountingPeriodFrom: String, accountingPeriodTo: String)(using hc: HeaderCarrier): Future[ORNRetrieveSuccessResponse] =
    connector.retrieveORN(accountingPeriodFrom, accountingPeriodTo).map(convertToRetrieveResult)

  private def convertToSubmitResult(response: HttpResponse): ORNSuccessResponse =
    response.status match {
      case 201 | 200 =>
        response.json.validate[ORNSuccessResponse](using ORNSuccessResponse.reads) match {
          case JsSuccess(success, _) => success
          case JsError(errors)       =>
            logger.error(s"Failed to parse success response. Errors: ${errors.toString()}")
            throw UnexpectedResponse
        }
      case 422 =>
        response.json.validate[ORNErrorResponse] match {
          case JsSuccess(response, _) => throw DownstreamValidationError(response.code, response.message)
          case JsError(_)             =>
            logger.error("Failed to parse unprocessible entity response")
            throw UnexpectedResponse
        }
      case status =>
        logger.error(s"Error calling pillar2 backend. Got response: $status")
        throw UnexpectedResponse
    }

  private def convertToRetrieveResult(response: HttpResponse): ORNRetrieveSuccessResponse =
    response.status match {
      case 200 =>
        logger.info(s"Received response body: ${response.body}")
        response.json.validate[ORNRetrieveSuccessResponse](using ORNRetrieveSuccessResponse.reads) match {
          case JsSuccess(success, _) =>
            logger.info(s"Successfully parsed response: $success")
            success
          case JsError(errors) =>
            logger.error(s"Failed to parse success response. Errors: ${errors.toString()}")
            logger.error(s"JSON structure: ${Json.prettyPrint(response.json)}")
            throw UnexpectedResponse
        }
      case 422 =>
        response.json.validate[ORNErrorResponse] match {
          case JsSuccess(response, _) =>
            if response.code == "005" && response.message.contains("No Form Bundle found") then {
              throw ORNNotFoundException
            } else {
              throw DownstreamValidationError(response.code, response.message)
            }
          case JsError(_) =>
            logger.error("Failed to parse unprocessible entity response")
            throw UnexpectedResponse
        }
      case status =>
        logger.error(s"Error calling pillar2 backend. Got response: $status")
        throw UnexpectedResponse
    }
}
