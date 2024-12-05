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

package uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.responses

import play.api.libs.json._

sealed trait ApiResponse

case class SubmitUktrSuccessResponse(processingDate: String, formBundleNumber: String, chargeReference: Option[String]) extends ApiResponse

case class SubmitUktrErrorResponse(code: String, message: String) extends ApiResponse

object ApiResponse {
  implicit val errorFormat:   OFormat[SubmitUktrErrorResponse]   = Json.format[SubmitUktrErrorResponse]
  implicit val successFormat: OFormat[SubmitUktrSuccessResponse] = Json.format[SubmitUktrSuccessResponse]
  implicit val apiResponseReads: Reads[ApiResponse] = (json: JsValue) =>
    json.validate[SubmitUktrSuccessResponse] match {
      case JsSuccess(_, _) =>
        json.validate[SubmitUktrSuccessResponse]
      case JsError(_) =>
        json.validate[SubmitUktrErrorResponse]
    }

  val internalServerError: SubmitUktrErrorResponse = SubmitUktrErrorResponse("500", "Internal server error")
}
