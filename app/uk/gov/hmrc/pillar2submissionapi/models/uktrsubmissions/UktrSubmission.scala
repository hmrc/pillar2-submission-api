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
  implicit val uktrSubmissionReads: Reads[UktrSubmission] = Reads { json =>
    val requiredTopLevelFields = Seq(
      "accountingPeriodFrom",
      "accountingPeriodTo",
      "obligationMTT",
      "electionUKGAAP",
      "liabilities"
    )

    val missingTopLevelFields = requiredTopLevelFields
      .filterNot(field => (json \ field).isDefined)
      .map(field => (JsPath \ field, Seq(JsonValidationError("error.path.missing"))))

    if (missingTopLevelFields.nonEmpty) {
      JsError(missingTopLevelFields)
    } else {
      (json \ "liabilities").validate[JsObject] match {
        case JsSuccess(liabilities, _) =>
          if ((liabilities \ "returnType").isDefined) {
            json.validate[UktrSubmissionNilReturn]
          } else if ((liabilities \ "electionDTTSingleMember").isDefined) {
            json.validate[UktrSubmissionData]
          } else {
            JsError(Seq((JsPath \ "liabilities", Seq(JsonValidationError("Unknown submission type")))))
          }

        case JsError(errors) =>
          JsError(Seq((JsPath \ "liabilities", Seq(JsonValidationError("error.path.missing")))) ++ errors)
      }
    }
  }
}
