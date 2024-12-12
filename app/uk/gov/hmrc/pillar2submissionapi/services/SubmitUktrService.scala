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
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.connectors.Pillar2Connector
import uk.gov.hmrc.pillar2submissionapi.controllers.error.{UktrValidationError, UnParsableResponse, UnexpectedResponse}
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.UktrSubmission
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.responses.{SubmitUktrErrorResponse, SubmitUktrSuccessResponse}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmitUktrService @Inject() (pillar2Connector: Pillar2Connector)(implicit val ec: ExecutionContext) {

  def submitUktr(request: UktrSubmission)(implicit hc: HeaderCarrier): Future[SubmitUktrSuccessResponse] =
    pillar2Connector.submitUktr(request).map(convertToResult)

  private def convertToResult(response: HttpResponse): SubmitUktrSuccessResponse =
    response.status match {
      case 201 =>
        response.json.validate[SubmitUktrSuccessResponse] match {
          case JsSuccess(success, _) => success
          case JsError(errors) =>
            throw UnParsableResponse("Failed to parse success response: " + errors.map(e => e._2.toString()))
        }
      case 422 =>
        response.json.validate[SubmitUktrErrorResponse] match {
          case JsSuccess(response, _) =>
            throw UktrValidationError(response.code, response.message)
          case JsError(errors) =>
            throw UnParsableResponse("Failed to parse error response: " + errors.map(e => e._2.toString()))
        }
      case _ =>
        throw UnexpectedResponse
    }
}
