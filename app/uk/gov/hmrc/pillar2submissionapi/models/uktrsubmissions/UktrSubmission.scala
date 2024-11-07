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

package uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions

import play.api.libs.json._

import java.time.LocalDate

trait UktrSubmission {
  val accountingPeriodFrom: LocalDate
  val accountingPeriodTo:   LocalDate
  val obligationMTT:        Boolean
  val electionUKGAAP:       Boolean
  val liabilities:          Liability
}

object UktrSubmission {
  implicit val uktrSubmissionDataFormat:      OFormat[UktrSubmissionData]      = Json.format[UktrSubmissionData]
  implicit val uktrSubmissionNilReturnFormat: OFormat[UktrSubmissionNilReturn] = Json.format[UktrSubmissionNilReturn]

  implicit val uktrSubmissionReads: Reads[UktrSubmission] = (json: JsValue) =>
    (json \ "liabilities").asOpt[LiabilityNilReturn] match {
      case Some(nilReturnRequest) =>
        if (nilReturnRequest.returnType == ReturnType.NilReturn) {
          json.validate[UktrSubmissionNilReturn]
        } else JsError("invalid return type")
      case None =>
        json.validate[UktrSubmissionData]
    }
}