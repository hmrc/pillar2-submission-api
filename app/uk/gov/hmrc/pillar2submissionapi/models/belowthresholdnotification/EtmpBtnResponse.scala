/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.pillar2submissionapi.models.belowthresholdnotification

import play.api.libs.json.{Json, OFormat}

case class EtmpBtnSuccess(processingDate: String)
object EtmpBtnSuccess {
  given format: OFormat[EtmpBtnSuccess] = Json.format[EtmpBtnSuccess]
}

case class EtmpBtnSuccessWrapper(success: EtmpBtnSuccess)
object EtmpBtnSuccessWrapper {
  given format: OFormat[EtmpBtnSuccessWrapper] = Json.format[EtmpBtnSuccessWrapper]
}

case class EtmpBtnError(code: String, text: String)
object EtmpBtnError {
  given format: OFormat[EtmpBtnError] = Json.format[EtmpBtnError]
}

case class EtmpBtnErrorWrapper(errors: EtmpBtnError)
object EtmpBtnErrorWrapper {
  given format: OFormat[EtmpBtnErrorWrapper] = Json.format[EtmpBtnErrorWrapper]
}
