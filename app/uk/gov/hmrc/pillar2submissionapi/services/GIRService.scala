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
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.pillar2submissionapi.connectors.GIRConnector
import uk.gov.hmrc.pillar2submissionapi.controllers.error.{DownstreamValidationError, UnexpectedResponse}
import uk.gov.hmrc.pillar2submissionapi.models.globeinformationreturn.GIRSubmission
import uk.gov.hmrc.pillar2submissionapi.models.globeinformationreturn.SubmitGIRErrorResponse
import uk.gov.hmrc.pillar2submissionapi.models.globeinformationreturn.SubmitGIRSuccessResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
@Singleton
class GIRService @Inject() (
  girConnector: GIRConnector
)(implicit ec: ExecutionContext) extends Logging {

  def createGIR(submission: GIRSubmission)(implicit
    hc:                                 HeaderCarrier
  ): Future[SubmitGIRSuccessResponse] = {
    girConnector.createGIR(submission).map(convertToResult)
  }

  private def convertToResult(response: HttpResponse): SubmitGIRSuccessResponse =
    response.status match {
      case 201 =>
        response.json.validate[SubmitGIRSuccessResponse] match {
          case JsSuccess(success, _) => success
          case JsError(e) =>
            logger.error(s"Error while parsing the backend response" + response.body + e)
            throw UnexpectedResponse
        }
      case 422 =>
        response.json.validate[SubmitGIRErrorResponse] match {
          case JsSuccess(response, _) => throw DownstreamValidationError(response.errors.code, response.errors.text)
          case JsError(e) =>
            logger.error(s"Error while unprocessable entity response" + response.body + e)
            throw UnexpectedResponse
        }
      case status =>
        logger.error(s"Error while calling pillar2 backend. Got status: $status")
        throw UnexpectedResponse
    }
}
