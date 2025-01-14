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
import play.api.http.Status.{CREATED, OK, UNPROCESSABLE_ENTITY}
import play.api.libs.json._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.connectors.UKTaxReturnConnector
import uk.gov.hmrc.pillar2submissionapi.controllers.error.{UktrValidationError, UnexpectedResponse}
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.UKTRSubmission
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.responses.{UKTRSubmitErrorResponse, UKTRSubmitSuccessResponse}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UKTaxReturnService @Inject() (ukTaxReturnConnector: UKTaxReturnConnector)(implicit val ec: ExecutionContext) extends Logging {

  def submitUKTR(request: UKTRSubmission)(implicit hc: HeaderCarrier): Future[UKTRSubmitSuccessResponse] =
    ukTaxReturnConnector.submitUKTR(request).map(convertToResult)

  def amendUKTR(request: UKTRSubmission)(implicit hc: HeaderCarrier): Future[UKTRSubmitSuccessResponse] =
    ukTaxReturnConnector.amendUKTR(request).map(convertToResult)

  private def convertToResult(response: HttpResponse): UKTRSubmitSuccessResponse = {
    def logAndThrow(errors: Seq[(JsPath, Seq[JsonValidationError])]): UKTRSubmitSuccessResponse = {
      val errorPath    = errors.map(_._1.path.headOption.fold("error.path.missing")(_.asInstanceOf[KeyPathNode].key))
      val errorMessage = errors.map(_._2.map(_.message).headOption.fold("error.message.missing")(identity))
      val zippedErrors =
        errorPath
          .zip(errorMessage)
          .map { case (path, message) => s"$path: $message" }
          .mkString("; ")
      logger.error(s"Error while parsing the backend response ${response.json} - ($zippedErrors)")
      throw UnexpectedResponse
    }

    response.status match {
      case CREATED | OK =>
        response.json.validate[UKTRSubmitSuccessResponse] match {
          case JsSuccess(success, _) => success
          case JsError(errors)       => logAndThrow(errors.asInstanceOf[Seq[(JsPath, Seq[JsonValidationError])]])
        }
      case UNPROCESSABLE_ENTITY =>
        response.json.validate[UKTRSubmitErrorResponse] match {
          case JsSuccess(response, _) => throw UktrValidationError(response.code, response.message)
          case JsError(errors)        => logAndThrow(errors.asInstanceOf[Seq[(JsPath, Seq[JsonValidationError])]])
        }
      case status =>
        logger.error(s"Error while calling pillar2 backend. Got status: $status and response: ${response.json}")
        throw UnexpectedResponse
    }
  }
}
