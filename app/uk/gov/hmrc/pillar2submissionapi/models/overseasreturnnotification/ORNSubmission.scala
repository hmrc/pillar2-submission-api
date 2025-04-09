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

package uk.gov.hmrc.pillar2submissionapi.models.overseasreturnnotification

import play.api.libs.json.{Json, JsonValidationError, OFormat, Reads}
import java.time.LocalDate

case class ORNSubmission(
  accountingPeriodFrom: LocalDate,
  accountingPeriodTo:   LocalDate,
  filedDateGIR:         LocalDate,
  countryGIR:           String,
  reportingEntityName:  String,
  TIN:                  String,
  issuingCountryTIN:    String
)

object ORNSubmission {

  private val countryGIRReads: Reads[String] =
    implicitly[Reads[String]].filter(JsonValidationError("countryGIR must be between 1 and 2 characters"))(str => str.length >= 1 && str.length <= 2)

  private val reportingEntityNameReads: Reads[String] =
    implicitly[Reads[String]].filter(JsonValidationError("reportingEntityName must be between 1 and 200 characters"))(str =>
      str.length >= 1 && str.length <= 200
    )

  private val tinReads: Reads[String] =
    implicitly[Reads[String]].filter(JsonValidationError("TIN must be between 1 and 200 characters"))(str => str.length >= 1 && str.length <= 200)

  private val issuingCountryTINReads: Reads[String] =
    implicitly[Reads[String]].filter(JsonValidationError("issuingCountryTIN must be between 1 and 2 characters"))(str =>
      str.length >= 1 && str.length <= 2
    )

  implicit val reads: Reads[ORNSubmission] = {
    import play.api.libs.functional.syntax._
    import play.api.libs.json._

    (
      (JsPath \ "accountingPeriodFrom").read[LocalDate] and
        (JsPath \ "accountingPeriodTo").read[LocalDate] and
        (JsPath \ "filedDateGIR").read[LocalDate] and
        (JsPath \ "countryGIR").read(countryGIRReads) and
        (JsPath \ "reportingEntityName").read(reportingEntityNameReads) and
        (JsPath \ "TIN").read(tinReads) and
        (JsPath \ "issuingCountryTIN").read(issuingCountryTINReads)
    )(ORNSubmission.apply _)
  }

  implicit val format: OFormat[ORNSubmission] = Json.format[ORNSubmission]
}
