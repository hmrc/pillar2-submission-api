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

package uk.gov.hmrc.pillar2submissionapi.controllers

import play.api.http.Status.{BAD_REQUEST, CREATED}
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, contentAsString, defaultAwaitTimeout, status}
import uk.gov.hmrc.pillar2submissionapi.controllers.UktrSubmissionControllerSpec._
import uk.gov.hmrc.pillar2submissionapi.controllers.base.ControllerBaseSpec

class UktrSubmissionControllerSpec extends ControllerBaseSpec {

  val uktrSubmissionController: UktrSubmissionController = new UktrSubmissionController(cc)

  "UktrSubmissionController" when {
    "submitUktr() called with a valid request" should {
      "return 201 CREATED response" in {
        assert {
          val result = uktrSubmissionController.submitUktr()(
            FakeRequest(method = "POST", path = "/RESTAdapter/PLR/UKTaxReturn")
              .withJsonBody(validRequestJson_data)
          )
          status(result) == CREATED
        }
      }
    }

    "submitUktr() called with a valid nil return request" should {
      "return 201 CREATED response" in {
        assert {
          val result = uktrSubmissionController.submitUktr()(
            FakeRequest(method = "POST", path = "/RESTAdapter/PLR/UKTaxReturn")
              .withJsonBody(validRequestJson_nilReturn)
          )
          status(result) == CREATED
        }
      }
    }

    "submitUktr() called with an invalid request" should {
      "return 400 BAD_REQUEST response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "POST", path = "/RESTAdapter/PLR/UKTaxReturn")
            .withJsonBody(invalidRequestJson_data)
            .withHeaders("Content-Type" -> "application/json")
        )

        assert(
          status(result) == BAD_REQUEST &&
            contentAsJson(result) == Json.obj(
              "errors" -> Json.arr(
                "totalLiability: totalLiability must be a positive number",
                "totalLiabilityDTT: totalLiabilityDTT must be a positive number",
                "totalLiabilityIIR: totalLiabilityIIR must be a positive number",
                "totalLiabilityUTPR: totalLiabilityUTPR must be a positive number",
                "ukChargeableEntityName: ukChargeableEntityName is missing or empty",
                "idType: idType is missing or empty",
                "idValue: idValue is missing or empty",
                "amountOwedDTT: amountOwedDTT must be a positive number",
                "amountOwedIIR: amountOwedIIR must be a positive number",
                "amountOwedUTPR: amountOwedUTPR must be a positive number"
              )
            )
        )
      }
    }

    "submitUktr() called with request containing invalid data types" should {
      "return 400 BAD_REQUEST response with validation errors" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "POST", path = "/RESTAdapter/PLR/UKTaxReturn")
            .withJsonBody(invalidRequestJson_data)
        )

        assert {
          status(result) == BAD_REQUEST &&
          contentAsJson(result) == Json.obj(
            "errors" -> Json.arr(
              "totalLiability: totalLiability must be a positive number",
              "totalLiabilityDTT: totalLiabilityDTT must be a positive number",
              "totalLiabilityIIR: totalLiabilityIIR must be a positive number",
              "totalLiabilityUTPR: totalLiabilityUTPR must be a positive number",
              "ukChargeableEntityName: ukChargeableEntityName is missing or empty",
              "idType: idType is missing or empty",
              "idValue: idValue is missing or empty",
              "amountOwedDTT: amountOwedDTT must be a positive number",
              "amountOwedIIR: amountOwedIIR must be a positive number",
              "amountOwedUTPR: amountOwedUTPR must be a positive number"
            )
          )
        }
      }
    }

    "submitUktr() called with request that is missing liabilities" should {
      "return 400 BAD_REQUEST response with specific error message" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "POST", path = "/RESTAdapter/PLR/UKTaxReturn")
            .withJsonBody(invalidRequest_noLiabilities)
        )

        assert {
          status(result) == BAD_REQUEST &&
          contentAsJson(result) == Json.obj(
            "message" -> "Invalid JSON format",
            "details" -> Json.arr("Path: , Errors: Invalid JSON format: missing required fields")
          )
        }
      }
    }
    "submitUktr() called with an empty request body" should {
      "return 400 BAD_REQUEST response with a specific message" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "POST", path = "/RESTAdapter/PLR/UKTaxReturn")
            .withJsonBody(invalidRequest_emptyBody)
        )

        assert {
          status(result) == BAD_REQUEST &&
          contentAsString(result).contains("Invalid JSON format")
        }
      }
    }

    "submitUktr() called with malformed JSON" should {
      "return 400 BAD_REQUEST response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "POST", path = "/RESTAdapter/PLR/UKTaxReturn")
            .withTextBody("This is invalid JSON.") // Malformed JSON
            .withHeaders("Content-Type" -> "application/json") // Explicitly mark as JSON
        )

        assert(
          status(result) == BAD_REQUEST &&
            contentAsString(result).contains("Invalid JSON format")
        )
      }
    }

    "submitUktr() called with an unknown submission type" should {
      "return 400 BAD_REQUEST response with 'Unknown submission type'" in {
        val invalidSubmission: JsValue = Json.parse("""{
                                                      |  "accountingPeriodFrom": "2024-08-14",
                                                      |  "accountingPeriodTo": "2024-12-14",
                                                      |  "obligationMTT": true,
                                                      |  "electionUKGAAP": true,
                                                      |  "liabilities": { "unexpectedField": "unexpectedValue" }
                                                      |}""".stripMargin)

        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "POST", path = "/RESTAdapter/PLR/UKTaxReturn")
            .withJsonBody(invalidSubmission)
        )

        assert(
          status(result) == BAD_REQUEST &&
            contentAsJson(result) == Json.obj("message" -> "Unknown submission type")
        )
      }
    }

    "submitUktr() called with a non-JSON body marked as JSON" should {
      "return 400 BAD_REQUEST response with 'Invalid JSON format'" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "POST", path = "/RESTAdapter/PLR/UKTaxReturn")
            .withTextBody("This is not JSON.")
            .withHeaders("Content-Type" -> "application/json")
        )

        assert(
          status(result) == BAD_REQUEST &&
            contentAsJson(result) == Json.obj("message" -> "Invalid JSON format")
        )
      }
    }

  }
}
object UktrSubmissionControllerSpec {
  val validRequestJson_data: JsValue =
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

  val validRequestJson_nilReturn: JsValue =
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

  val invalidRequestJson_data: JsValue =
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
                 |    "totalLiability": -100,
                 |    "totalLiabilityDTT": -500,
                 |    "totalLiabilityIIR": -300,
                 |    "totalLiabilityUTPR": -200,
                 |    "liableEntities": [
                 |      {
                 |        "ukChargeableEntityName": "",
                 |        "idType": "",
                 |        "idValue": "",
                 |        "amountOwedDTT": -1000,
                 |        "amountOwedIIR": -800,
                 |        "amountOwedUTPR": -600
                 |      }
                 |    ]
                 |  }
                 |}""".stripMargin)

  val invalidRequest_noLiabilities: JsValue =
    Json.parse("""{
                 |  "accountingPeriodFrom": "2024-08-14",
                 |  "accountingPeriodTo": "2024-12-14",
                 |  "obligationMTT": true
                 |}""".stripMargin)

  val invalidRequest_emptyBody: JsValue = JsObject.empty
}
