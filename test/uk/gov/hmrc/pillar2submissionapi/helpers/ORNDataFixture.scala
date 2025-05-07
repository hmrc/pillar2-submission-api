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

package uk.gov.hmrc.pillar2submissionapi.helpers

import play.api.libs.json._
import uk.gov.hmrc.pillar2submissionapi.models.overseasreturnnotification.{ORNSubmission, ORNSubmitSuccessResponse, ORNSuccessResponse}

import java.time.LocalDate

trait ORNDataFixture {

  val ornRequestFixture: ORNSubmission = ORNSubmission(
    accountingPeriodFrom = LocalDate.now(),
    accountingPeriodTo = LocalDate.now().plusYears(1),
    filedDateGIR = LocalDate.now().minusDays(10),
    countryGIR = "US",
    reportingEntityName = "Newco PLC",
    TIN = "US12345678",
    issuingCountryTIN = "US"
  )
  val ornRequestJs: JsValue = Json.toJson(ornRequestFixture)

  // Response for Retrieve operations (GET)
  val retrieveOrnResponse: ORNSuccessResponse = ORNSuccessResponse(
    processingDate = "2022-01-31T09:26:17Z",
    accountingPeriodFrom = "2024-01-01",
    accountingPeriodTo = "2024-12-31",
    filedDateGIR = "2025-01-10",
    countryGIR = "US",
    reportingEntityName = "Newco PLC",
    TIN = "US12345678",
    issuingCountryTIN = "US"
  )

  // Response for Submit and Amend operations (POST/PUT)
  val submitOrnResponse: ORNSubmitSuccessResponse = ORNSubmitSuccessResponse(
    processingDate = "2022-01-31T09:26:17Z",
    formBundleNumber = "123456789012345"
  )

  val invalidRequestJson_data: JsValue = ornRequestJs.as[JsObject] - "filedDateGIR" - "TIN" // Remove fields to make the JSON invalid
  val invalidRequest_Json: JsValue =
    ornRequestJs.as[JsObject] + ("accountingPeriodFrom" -> JsString("invalid-date"))
  val invalidRequest_emptyBody: JsValue = JsObject.empty
  val invalidRequest_wrongType: String  = "This is not Json."
  val validRequestJson_duplicateFields: JsValue =
    ornRequestJs.as[JsObject] + ("accountingPeriodFrom" -> JsString("2023-01-01"))
  val validRequestJson_additionalFields: JsValue =
    ornRequestJs.as[JsObject] + ("extraField" -> JsString("extraValue"))
  val invalidCountryGIRJson:          JsObject = ornRequestJs.as[JsObject] + ("countryGIR"          -> JsString("USA"))
  val invalidIssuingCountryTINJson:   JsObject = ornRequestJs.as[JsObject] + ("issuingCountryTIN"   -> JsString("USA"))
  val invalidReportingEntityNameJson: JsObject = ornRequestJs.as[JsObject] + ("reportingEntityName" -> JsString(""))
  val invalidTinJson:                 JsObject = ornRequestJs.as[JsObject] + ("TIN"                 -> JsString(""))
  val longString:                     String   = "a" * 201
  val invalidLongReportingEntityJson: JsObject = ornRequestJs.as[JsObject] + ("reportingEntityName" -> JsString(longString))
  val longTin:                        String   = "a" * 201
  val invalidLongTinJson:             JsObject = ornRequestJs.as[JsObject] + ("TIN"                 -> JsString(longTin))
}
