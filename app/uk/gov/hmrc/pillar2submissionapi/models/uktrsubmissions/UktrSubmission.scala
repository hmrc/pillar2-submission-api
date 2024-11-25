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

sealed trait UktrSubmission {
  val accountingPeriodFrom: LocalDate
  val accountingPeriodTo:   LocalDate
  val obligationMTT:        Boolean
  val electionUKGAAP:       Boolean
  val liabilities:          Liability
}

object UktrSubmission {
  implicit val uktrSubmissionReads: Reads[UktrSubmission] = Reads { json =>
    (json \ "liabilities" \ "returnType").validateOpt[String] match {
      case JsSuccess(Some(returnTypeStr), _) =>
        ReturnType.withNameOption(returnTypeStr) match {
          case Some(ReturnType.NIL_RETURN) =>
            json.validate[UktrSubmissionNilReturn]
          case None =>
            JsError(JsPath \ "liabilities" \ "returnType", JsonValidationError(s"Unknown submission type: $returnTypeStr"))
          case Some(_) =>
            json.validate[UktrSubmissionData]
        }
      case JsSuccess(None, _) =>
        // No returnType field, so assume it's UktrSubmissionData
        json.validate[UktrSubmissionData]
      case JsError(_) =>
        // Can't read returnType, try to parse as UktrSubmissionData or UktrSubmissionNilReturn
        json.validate[UktrSubmissionData].orElse(json.validate[UktrSubmissionNilReturn])
    }
  }

  implicit val writes: Writes[UktrSubmission] = Writes[UktrSubmission] {
    case data:      UktrSubmissionData      => Json.toJson(data)
    case nilReturn: UktrSubmissionNilReturn => Json.toJson(nilReturn)
  }
}

case class UktrSubmissionData(
  accountingPeriodFrom: LocalDate,
  accountingPeriodTo:   LocalDate,
  obligationMTT:        Boolean,
  electionUKGAAP:       Boolean,
  liabilities:          LiabilityData
) extends UktrSubmission

object UktrSubmissionData {
  implicit val uktrSubmissionDataFormat: OFormat[UktrSubmissionData] = Json.format[UktrSubmissionData]
}

case class UktrSubmissionNilReturn(
  accountingPeriodFrom: LocalDate,
  accountingPeriodTo:   LocalDate,
  obligationMTT:        Boolean,
  electionUKGAAP:       Boolean,
  liabilities:          LiabilityNilReturn
) extends UktrSubmission

object UktrSubmissionNilReturn {
  implicit val uktrSubmissionNilReturnFormat: OFormat[UktrSubmissionNilReturn] = Json.format[UktrSubmissionNilReturn]
}
