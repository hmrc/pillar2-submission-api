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
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import uk.gov.hmrc.pillar2submissionapi.controllers.UktrSubmissionControllerSpec._
import uk.gov.hmrc.pillar2submissionapi.controllers.base.ControllerBaseSpec

class UktrSubmissionControllerSpec extends ControllerBaseSpec {

  val uktrSubmissionController: UktrSubmissionController = new UktrSubmissionController(cc)

  "UktrSubmissionController" when {
    "submitUktr() called with a valid request" should {
      "return 201 CREATED response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "", path = "")
            .withJsonBody(validRequestJson_data)
        )
        status(result) mustEqual CREATED
      }
    }

    "submitUktr() called with a valid nil return request" should {
      "return 201 CREATED response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "", path = "")
            .withJsonBody(validRequestJson_nilReturn)
        )
        status(result) mustEqual CREATED
      }
    }

    "submitUktr() called with an invalid request" should {
      "return 400 BAD_REQUEST response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "", path = "")
            .withJsonBody(invalidRequestJson_data)
        )
        status(result) mustEqual BAD_REQUEST
      }
    }

    "submitUktr() called with an invalid nil return request" should {
      "return 400 BAD_REQUEST response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "", path = "")
            .withJsonBody(invalidRequestJson_nilReturn)
        )
        status(result) mustEqual BAD_REQUEST
      }
    }

    "submitUktr() called with an invalid json request" should {
      "return 400 BAD_REQUEST response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "", path = "")
            .withJsonBody(invalidRequest_Json)
        )
        status(result) mustEqual BAD_REQUEST
      }
    }

    "submitUktr() called with request that only contains a valid return type" should {
      "return 400 BAD_REQUEST response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "", path = "")
            .withJsonBody(invalidRequest_nilReturn_onlyContainsLiabilities)
        )
        status(result) mustEqual BAD_REQUEST
      }
    }

    "submitUktr() called with request that only contains an invalid return type" should {
      "return 400 BAD_REQUEST response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "", path = "")
            .withJsonBody(invalidRequest_nilReturn_onlyLiabilitiesButInvalidReturnType)
        )
        status(result) mustEqual BAD_REQUEST
      }
    }

    "submitUktr() called with request that is missing liabilities" should {
      "return 400 BAD_REQUEST response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "", path = "")
            .withJsonBody(invalidRequest_noLiabilities)
        )
        status(result) mustEqual BAD_REQUEST
      }
    }

    "submitUktr() called with an empty request body" should {
      "return 400 BAD_REQUEST response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "", path = "")
            .withJsonBody(invalidRequest_emptyBody)
        )
        status(result) mustEqual BAD_REQUEST
      }
    }

    "submitUktr() called with an non-json request" should {
      "return 400 BAD_REQUEST response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "", path = "")
            .withTextBody(invalidRequest_wrongType)
        )
        status(result) mustEqual BAD_REQUEST
      }
    }

    "submitUktr() called with no request body" should {
      "return 400 BAD_REQUEST response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "", path = "")
        )
        status(result) mustEqual BAD_REQUEST
      }
    }

    "submitUktr() called with valid request body that contains duplicate entries" should {
      "return 201 CREATED response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "", path = "")
            .withJsonBody(validRequestJson_duplicateFields)
        )
        status(result) mustEqual CREATED
      }
    }

    "submitUktr() called with valid request body that contains additional fields" should {
      "return 201 CREATED response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "", path = "")
            .withJsonBody(validRequestJson_additionalFields)
        )
        status(result) mustEqual CREATED
      }
    }

    "submitUktr() called with valid request body that contains both a full and a nil submission" should {
      "return 201 CREATED response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "", path = "")
            .withJsonBody(validRequestJson_fullAndNilReturnTogether)
        )
        status(result) mustEqual CREATED
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

  val invalidRequestJson_nilReturn: JsValue =
    Json.parse("""{
      |  "accountingPeriodFrom": "2024-08-14",
      |  "accountingPeriodTo": "2024-12-14",
      |  "obligationMTT": true,
      |  "liabilities": {
      |    "returnType": "INVALID"
      |  }
      |}""".stripMargin)

  val invalidRequest_Json: JsValue =
    Json.parse("""{
        |  "badRequest": ""
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

  val invalidRequest_wrongType: String = "This is not Json."

  val invalidRequest_emptyBody: JsValue = JsObject.empty

  val validRequestJson_duplicateFields: JsValue =
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

  val validRequestJson_additionalFields: JsValue =
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

  val validRequestJson_fullAndNilReturnTogether: JsValue =
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
