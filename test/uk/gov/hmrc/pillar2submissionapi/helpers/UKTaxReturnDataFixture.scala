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

import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.ReturnType.NIL_RETURN
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.responses.UKTRSubmitSuccessResponse
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.{LiabilityData, LiabilityNilReturn, LiableEntity}

trait UKTaxReturnDataFixture {

  val pillar2Id        = "XTC01234123412"
  val formBundleNumber = "119000004320"
  val processingDate   = "2024-01-01T00:00:00Z"
  val uktrSubmissionSuccessResponse: UKTRSubmitSuccessResponse =
    UKTRSubmitSuccessResponse(processingDate, formBundleNumber, Some(pillar2Id))

  val liabilityNilReturn: LiabilityNilReturn = LiabilityNilReturn(NIL_RETURN)
  val liableEntity:       LiableEntity       = LiableEntity("entityName", "idType", "idValue", 1.1, 2.2, 3.3)
  val liabilityData: LiabilityData =
    LiabilityData(electionDTTSingleMember = true, electionUTPRSingleMember = false, 1, 2, 3.3, 4.4, 5.5, 6.6, Seq(liableEntity))

  val validLiabilityReturn: JsValue =
    Json.parse("""{
                 |  "accountingPeriodFrom": "2024-08-14",
                 |  "accountingPeriodTo": "2024-12-14",
                 |  "obligationMTT": true,
                 |  "electionUKGAAP": true,
                 |  "liabilities": {
                 |    "electionDTTSingleMember": false,
                 |    "electionUTPRSingleMember": false,
                 |    "numberSubGroupDTT": 1,
                 |    "numberSubGroupUTPR": 1,
                 |    "totalLiability": 10000.99,
                 |    "totalLiabilityDTT": 5000.99,
                 |    "totalLiabilityIIR": 4000,
                 |    "totalLiabilityUTPR": 10000.99,
                 |    "liableEntities": [
                 |      {
                 |        "ukChargeableEntityName": "Newco PLC",
                 |        "idType": "CRN",
                 |        "idValue": "12345678",
                 |        "amountOwedDTT": 5000,
                 |        "amountOwedIIR": 3400,
                 |        "amountOwedUTPR": 6000.5
                 |      }
                 |    ]
                 |  }
                 |}""".stripMargin)

  val validNilReturn: JsValue =
    Json.parse("""{
                 |  "accountingPeriodFrom": "2024-08-14",
                 |  "accountingPeriodTo": "2024-09-14",
                 |  "obligationMTT": true,
                 |  "electionUKGAAP": true,
                 |  "liabilities": {
                 |    "returnType": "NIL_RETURN"
                 |  }
                 |}
                 |""".stripMargin)

  val liabilityReturnInvalidLiabilities: JsValue =
    Json.parse("""{
                 |  "accountingPeriodFrom": "2024-08-14",
                 |  "accountingPeriodTo": "2024-12-14",
                 |  "obligationMTT": true,
                 |  "liabilities": {
                 |    "totalLiability": "these",
                 |    "totalLiabilityDTT": "shouldnt",
                 |    "totalLiabilityIIR": "be",
                 |    "totalLiabilityUTPR": "strings",
                 |    "liableEntities": [
                 |      {
                 |        "ukChargeableEntityName": "Newco PLC",
                 |        "idType": "CRN",
                 |        "idValue": "12345678",
                 |        "amountOwedDTT": 5000,
                 |        "electedDTT": true,
                 |        "amountOwedIIR": 3400,
                 |        "amountOwedUTPR": 6000.5,
                 |        "electedUTPR": true
                 |      }
                 |    ]
                 |  }
                 |}""".stripMargin)

  val nilReturnInvalidReturnType: JsValue =
    Json.parse("""{
                 |  "accountingPeriodFrom": "2024-08-14",
                 |  "accountingPeriodTo": "2024-12-14",
                 |  "obligationMTT": true,
                 |  "liabilities": {
                 |    "returnType": "INVALID"
                 |  }
                 |}""".stripMargin)

  val invalidRequest_nilReturn_onlyContainsLiabilities: JsValue =
    Json.parse("""{
                 |  "liabilities": {
                 |    "returnType": "NIL_RETURN"
                 |  }
                 |}
                 |""".stripMargin)

  val invalidRequest_nilReturn_onlyLiabilitiesButInvalidReturnType: JsValue =
    Json.parse("""{
                 |  "liabilities": {
                 |    "returnType": "INVALID"
                 |  }
                 |}
                 |""".stripMargin)

  val invalidRequest_noLiabilities: JsValue =
    Json.parse("""{
                 |  "accountingPeriodFrom": "2024-08-14",
                 |  "accountingPeriodTo": "2024-12-14",
                 |  "obligationMTT": true
                 |}""".stripMargin)

  val stringBody:  String  = "This is not Json."
  val emptyBody:   JsValue = JsObject.empty
  val invalidBody: JsValue = Json.parse("""{"badRequest": ""}""")

  val liabilityReturnDuplicateFields: JsValue =
    Json.parse("""{
                 |  "accountingPeriodFrom": "2024-08-14",
                 |  "accountingPeriodTo": "2024-12-14",
                 |  "obligationMTT": true,
                 |  "obligationMTT": true,
                 |  "electionUKGAAP": true,
                 |  "liabilities": {
                 |    "electionDTTSingleMember": false,
                 |    "electionUTPRSingleMember": false,
                 |    "numberSubGroupDTT": 1,
                 |    "numberSubGroupUTPR": 1,
                 |    "totalLiability": 10000.99,
                 |    "totalLiabilityDTT": 5000.99,
                 |    "totalLiabilityIIR": 4000,
                 |    "totalLiabilityUTPR": 10000.99,
                 |    "totalLiabilityUTPR": 10000.99,
                 |    "liableEntities": [
                 |      {
                 |        "ukChargeableEntityName": "Newco PLC",
                 |        "idType": "CRN",
                 |        "idValue": "12345678",
                 |        "amountOwedDTT": 5000,
                 |        "amountOwedIIR": 3400,
                 |        "amountOwedUTPR": 6000.5
                 |      },
                 |      {
                 |        "ukChargeableEntityName": "Newco PLC",
                 |        "idType": "CRN",
                 |        "idValue": "12345678",
                 |        "amountOwedDTT": 5000,
                 |        "amountOwedIIR": 3400,
                 |        "amountOwedUTPR": 6000.5
                 |      }
                 |    ]
                 |  }
                 |}""".stripMargin)

  val liabilityReturnWithadditionalFields: JsValue =
    Json.parse("""{
                 |  "accountingPeriodFrom": "2024-08-14",
                 |  "accountingPeriodTo": "2024-12-14",
                 |  "obligationMTT": true,
                 |  "electionUKGAAP": true,
                 |  "extraField": "this should not be here",
                 |  "liabilities": {
                 |    "electionDTTSingleMember": false,
                 |    "electionUTPRSingleMember": false,
                 |    "numberSubGroupDTT": 1,
                 |    "numberSubGroupUTPR": 1,
                 |    "totalLiability": 10000.99,
                 |    "totalLiabilityDTT": 5000.99,
                 |    "totalLiabilityIIR": 4000,
                 |    "totalLiabilityUTPR": 10000.99,
                 |    "liableEntities": [
                 |      {
                 |        "ukChargeableEntityName": "Newco PLC",
                 |        "idType": "CRN",
                 |        "idValue": "12345678",
                 |        "amountOwedDTT": 5000,
                 |        "amountOwedIIR": 3400,
                 |        "amountOwedUTPR": 6000.5
                 |      }
                 |    ]
                 |  }
                 |}""".stripMargin)

  val liabilityAndNilReturn: JsValue =
    Json.parse("""{
                 |  "accountingPeriodFrom": "2024-08-14",
                 |  "accountingPeriodTo": "2024-12-14",
                 |  "obligationMTT": true,
                 |  "electionUKGAAP": true,
                 |  "liabilities": {
                 |    "electionDTTSingleMember": false,
                 |    "electionUTPRSingleMember": false,
                 |    "numberSubGroupDTT": 1,
                 |    "numberSubGroupUTPR": 1,
                 |    "totalLiability": 10000.99,
                 |    "totalLiabilityDTT": 5000.99,
                 |    "totalLiabilityIIR": 4000,
                 |    "totalLiabilityUTPR": 10000.99,
                 |    "liableEntities": [
                 |      {
                 |        "ukChargeableEntityName": "Newco PLC",
                 |        "idType": "CRN",
                 |        "idValue": "12345678",
                 |        "amountOwedDTT": 5000,
                 |        "amountOwedIIR": 3400,
                 |        "amountOwedUTPR": 6000.5
                 |      }
                 |    ]
                 |  },
                 |  "liabilities": {
                 |    "returnType": "NIL_RETURN"
                 |  }
                 |}""".stripMargin)
}

object UKTRErrorCodes {
  val REGIME_MISSING_OR_INVALID_001                             = "001"
  val PILLAR_2_ID_MISSING_OR_INVALID_002                        = "002"
  val REQUEST_COULD_NOT_BE_PROCESSED_003                        = "003"
  val DUPLICATE_SUBMISSION_ACKNOWLEDGMENT_REFERENCE_004         = "004"
  val BUSINESS_PARTNER_DOES_NOT_HAVE_AN_ACTIVE_SUBSCRIPTION_007 = "007"
  val TAX_OBLIGATION_ALREADY_FULFILLED_044                      = "044"
  val INVALID_RETURN_093                                        = "093"
  val INVALID_DTT_ELECTION_094                                  = "094"
  val INVALID_UTPR_ELECTION_095                                 = "095"
  val INVALID_TOTAL_LIABILITY_096                               = "096"
  val INVALID_TOTAL_LIABILITY_IIR_097                           = "097"
  val INVALID_TOTAL_LIABILITY_DTT_098                           = "098"
  val INVALID_TOTAL_LIABILITY_UTPR_099                          = "099"
  val BAD_REQUEST_400                                           = "400"
  val UNPROCESSABLE_CONTENT_422                                 = "422"
  val INTERNAL_SERVER_ERROR_500                                 = "500"
}
