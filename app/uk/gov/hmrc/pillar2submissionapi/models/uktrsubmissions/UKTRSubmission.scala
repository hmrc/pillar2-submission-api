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

sealed trait UKTRSubmission {
  val accountingPeriodFrom: LocalDate
  val accountingPeriodTo:   LocalDate
  val obligationMTT:        Boolean
  val electionUKGAAP:       Boolean
  val liabilities:          Liability
}

case class UKTRSubmissionData(
  accountingPeriodFrom: LocalDate,
  accountingPeriodTo:   LocalDate,
  obligationMTT:        Boolean,
  electionUKGAAP:       Boolean,
  liabilities:          LiabilityData
) extends UKTRSubmission

object UKTRSubmissionData {
  given uktrSubmissionDataFormat: OFormat[UKTRSubmissionData] = Json.format[UKTRSubmissionData]
}

case class UKTRSubmissionNilReturn(
  accountingPeriodFrom: LocalDate,
  accountingPeriodTo:   LocalDate,
  obligationMTT:        Boolean,
  electionUKGAAP:       Boolean,
  liabilities:          LiabilityNilReturn
) extends UKTRSubmission

object UKTRSubmissionNilReturn {
  given uktrSubmissionNilReturnFormat: OFormat[UKTRSubmissionNilReturn] = Json.format[UKTRSubmissionNilReturn]
}

object UKTRSubmission {
  given uktrSubmissionReads: Reads[UKTRSubmission] = (json: JsValue) =>
    if ((json \ "liabilities" \ "returnType").isEmpty) {
      json.validate[UKTRSubmissionData]
    } else {
      json.validate[UKTRSubmissionNilReturn]
    }

  given uktrSubmissionWrites: Writes[UKTRSubmission] = new Writes[UKTRSubmission] {
    def writes(sub: UKTRSubmission): JsValue = sub match {
      case submission @ UKTRSubmissionData(_, _, _, _, _) =>
        Json.toJson(submission)
      case nilReturn @ UKTRSubmissionNilReturn(_, _, _, _, _) =>
        Json.toJson(nilReturn)
    }
  }
}
