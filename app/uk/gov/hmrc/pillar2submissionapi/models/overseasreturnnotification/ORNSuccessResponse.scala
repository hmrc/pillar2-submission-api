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

import play.api.libs.json.*

case class ORNRetrieveSuccessResponse(
  processingDate:       String,
  accountingPeriodFrom: String,
  accountingPeriodTo:   String,
  filedDateGIR:         String,
  countryGIR:           String,
  reportingEntityName:  String,
  TIN:                  String,
  issuingCountryTIN:    String
)

case class ORNSuccessResponse(processingDate: String, formBundleNumber: String)

object ORNRetrieveSuccessResponse {

  given reads: Reads[ORNRetrieveSuccessResponse] = (json: JsValue) => {
    val standardReads = Json.reads[ORNRetrieveSuccessResponse]
    standardReads.reads(json) match {
      case success: JsSuccess[?] => success.asInstanceOf[JsSuccess[ORNRetrieveSuccessResponse]]
      case _ =>
        (json \ "success").validate[ORNRetrieveSuccessResponse](using standardReads)
    }
  }

  given writes: OWrites[ORNRetrieveSuccessResponse] = Json.writes[ORNRetrieveSuccessResponse]

  given successFormat: OFormat[ORNRetrieveSuccessResponse] = OFormat(reads, writes)
}

object ORNSuccessResponse {
  given reads: Reads[ORNSuccessResponse] = (json: JsValue) => {
    val standardReads = Json.reads[ORNSuccessResponse]
    standardReads.reads(json) match {
      case success: JsSuccess[?] => success.asInstanceOf[JsSuccess[ORNSuccessResponse]]
      case _ =>
        (json \ "success").validate[ORNSuccessResponse](using standardReads)
    }
  }

  given writes: OWrites[ORNSuccessResponse] = Json.writes[ORNSuccessResponse]

  given submitSuccessFormat: OFormat[ORNSuccessResponse] = OFormat(reads, writes)
}
