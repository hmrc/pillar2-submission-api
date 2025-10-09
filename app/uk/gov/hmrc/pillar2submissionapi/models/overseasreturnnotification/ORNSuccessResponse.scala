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

import play.api.libs.json._

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

  implicit val reads: Reads[ORNRetrieveSuccessResponse] = (json: JsValue) => {
    val standardReads = Json.reads[ORNRetrieveSuccessResponse]
    standardReads.reads(json) match {
      case success: JsSuccess[_] => success.asInstanceOf[JsSuccess[ORNRetrieveSuccessResponse]]
      case _ =>
        (json \ "success").validate[ORNRetrieveSuccessResponse](using standardReads)
    }
  }

  implicit val writes: OWrites[ORNRetrieveSuccessResponse] = Json.writes[ORNRetrieveSuccessResponse]

  implicit val successFormat: OFormat[ORNRetrieveSuccessResponse] = OFormat(reads, writes)
}

object ORNSuccessResponse {
  implicit val reads: Reads[ORNSuccessResponse] = (json: JsValue) => {
    val standardReads = Json.reads[ORNSuccessResponse]
    standardReads.reads(json) match {
      case success: JsSuccess[_] => success.asInstanceOf[JsSuccess[ORNSuccessResponse]]
      case _ =>
        (json \ "success").validate[ORNSuccessResponse](using standardReads)
    }
  }

  implicit val writes: OWrites[ORNSuccessResponse] = Json.writes[ORNSuccessResponse]

  implicit val submitSuccessFormat: OFormat[ORNSuccessResponse] = OFormat(reads, writes)
}
