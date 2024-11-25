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
import play.api.libs.functional.syntax._

import java.time.LocalDate

sealed trait UktrSubmission {
  val accountingPeriodFrom: LocalDate
  val accountingPeriodTo:   LocalDate
  val obligationMTT:        Boolean
  val electionUKGAAP:       Boolean
  val liabilities:          Liability
}

object UktrSubmission {

  private val liabilityReads: Reads[Liability] = Reads[Liability] { json =>
    (json \ "returnType").validateOpt[String].flatMap {
      case Some("NIL_RETURN") =>
        json.validate[LiabilityNilReturn]
      case Some(invalidReturnType) =>
        JsError((JsPath \ "returnType") -> JsonValidationError(s"Unknown submission type: $invalidReturnType"))
      case None =>
        json.validate[LiabilityData]
    }
  }

//  private val liabilityReads: Reads[Liability] = Reads[Liability] { json =>
//    (json \ "returnType").validateOpt[String].flatMap {
//      case Some("NIL_RETURN") =>
//        json.validate[LiabilityNilReturn]
//      case Some(invalidReturnType) =>
//        JsError((JsPath \ "returnType") -> JsonValidationError(s"Unknown return type: $invalidReturnType"))
//      case None =>
//        json.validate[LiabilityData]
//    }
//  }

  implicit val uktrSubmissionReads: Reads[UktrSubmission] = (
    (JsPath \ "accountingPeriodFrom").read[LocalDate] and
      (JsPath \ "accountingPeriodTo").read[LocalDate] and
      (JsPath \ "obligationMTT").read[Boolean] and
      (JsPath \ "electionUKGAAP").read[Boolean] and
      (JsPath \ "liabilities").read[Liability](liabilityReads)
  )((accountingPeriodFrom, accountingPeriodTo, obligationMTT, electionUKGAAP, liabilities) =>
    liabilities match {
      case data: LiabilityData =>
        UktrSubmissionData(
          accountingPeriodFrom,
          accountingPeriodTo,
          obligationMTT,
          electionUKGAAP,
          data
        )
      case nilReturn: LiabilityNilReturn =>
        UktrSubmissionNilReturn(
          accountingPeriodFrom,
          accountingPeriodTo,
          obligationMTT,
          electionUKGAAP,
          nilReturn
        )
      case _ =>
        // Handle unexpected Liability subtype
        throw new IllegalArgumentException("Unknown Liability type")
    }
  )

  implicit val uktrSubmissionWrites: Writes[UktrSubmission] = Writes[UktrSubmission] {
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
