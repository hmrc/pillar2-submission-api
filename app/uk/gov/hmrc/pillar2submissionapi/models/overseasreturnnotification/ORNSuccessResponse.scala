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

case class ORNSuccessResponse(
  processingDate:       String,
  accountingPeriodFrom: String,
  accountingPeriodTo:   String,
  filedDateGIR:         String,
  countryGIR:           String,
  reportingEntityName:  String,
  TIN:                  String,
  issuingCountryTIN:    String
)

case class ORNSubmitSuccessResponse(
  processingDate:   String,
  formBundleNumber: String
)

object ORNSuccessResponse {

  implicit val reads: Reads[ORNSuccessResponse] = new Reads[ORNSuccessResponse] {
    override def reads(json: JsValue): JsResult[ORNSuccessResponse] = {
      val standardReads = Json.reads[ORNSuccessResponse]
      standardReads.reads(json) match {
        case success: JsSuccess[ORNSuccessResponse] => success
        case _ =>
          (json \ "success").validate[ORNSuccessResponse](standardReads)
      }
    }
  }

  implicit val writes: OWrites[ORNSuccessResponse] = Json.writes[ORNSuccessResponse]

  implicit val successFormat: OFormat[ORNSuccessResponse] = OFormat(reads, writes)
}

object ORNSubmitSuccessResponse {
  implicit val reads: Reads[ORNSubmitSuccessResponse] = new Reads[ORNSubmitSuccessResponse] {
    override def reads(json: JsValue): JsResult[ORNSubmitSuccessResponse] = {
      val standardReads = Json.reads[ORNSubmitSuccessResponse]
      standardReads.reads(json) match {
        case success: JsSuccess[ORNSubmitSuccessResponse] => success
        case _ =>
          (json \ "success").validate[ORNSubmitSuccessResponse](standardReads)
      }
    }
  }

  implicit val writes: OWrites[ORNSubmitSuccessResponse] = Json.writes[ORNSubmitSuccessResponse]

  implicit val submitSuccessFormat: OFormat[ORNSubmitSuccessResponse] = OFormat(reads, writes)
}
