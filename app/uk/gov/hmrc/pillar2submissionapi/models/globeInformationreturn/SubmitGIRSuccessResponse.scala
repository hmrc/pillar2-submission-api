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

package uk.gov.hmrc.pillar2submissionapi.models.globeinformationreturn

import play.api.libs.json.{Json, OFormat}

case class SubmitGIRSuccessResponse(success: GIRSuccess)

object SubmitGIRSuccessResponse {
  implicit val format: OFormat[SubmitGIRSuccessResponse] = Json.format[SubmitGIRSuccessResponse]
}

case class GIRSuccess(processingDate: String)

object GIRSuccess {
  implicit val format: OFormat[GIRSuccess] = Json.format[GIRSuccess]
}
