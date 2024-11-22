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
  implicit val uktrSubmissionReads: Reads[UktrSubmission] = (json: JsValue) =>
    if ((json \ "liabilities" \ "returnType").isDefined) {
      json.validate[UktrSubmissionNilReturn]
    } else if ((json \ "liabilities" \ "electionDTTSingleMember").isDefined) {
      json.validate[UktrSubmissionData]
    } else {
      // Handle unknown submission types gracefully
      val accountingPeriodFrom = (json \ "accountingPeriodFrom").asOpt[LocalDate]
      val accountingPeriodTo   = (json \ "accountingPeriodTo").asOpt[LocalDate]
      val obligationMTT        = (json \ "obligationMTT").asOpt[Boolean]
      val electionUKGAAP       = (json \ "electionUKGAAP").asOpt[Boolean]

      (accountingPeriodFrom, accountingPeriodTo, obligationMTT, electionUKGAAP) match {
        case (Some(from), Some(to), Some(mtt), Some(gaap)) =>
          JsSuccess(new UktrSubmission {
            override val accountingPeriodFrom: LocalDate = from
            override val accountingPeriodTo:   LocalDate = to
            override val obligationMTT:        Boolean   = mtt
            override val electionUKGAAP:       Boolean   = gaap
            override val liabilities:          Liability = null // Unknown liabilities
          })
        case _ =>
          JsError("Invalid JSON format: missing required fields")
      }
    }
}

//object UktrSubmission {
//  implicit val uktrSubmissionReads: Reads[UktrSubmission] = (json: JsValue) =>
//    if ((json \ "liabilities" \ "returnType").isEmpty) {
//      json.validate[UktrSubmissionData]
//    } else {
//      json.validate[UktrSubmissionNilReturn]
//    }
//}
