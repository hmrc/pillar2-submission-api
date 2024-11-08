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

import base.ControllerBaseSpec
import play.api.http.Status.{BAD_REQUEST, CREATED}
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, AnyContentAsJson}
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import uk.gov.hmrc.pillar2submissionapi.controllers.UktrSubmissionControllerSpec._

class UktrSubmissionControllerSpec extends ControllerBaseSpec {

  val uktrSubmissionController: UktrSubmissionController = new UktrSubmissionController(cc)

  "UktrSubmissionController" when {
    "submitUktr() called with a valid request" should {
      "return 201 CREATED response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "", path = "")
            .withBody(validRequestJson_data)
        )
        status(result) mustEqual CREATED
      }
    }

    "submitUktr() called with a valid nil return request" should {
      "return 201 CREATED response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "", path = "")
            .withBody(validRequestJson_nilReturn)
        )
        status(result) mustEqual CREATED
      }
    }

    "submitUktr() called with an invalid request" should {
      "return 400 BAD_REQUEST response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "", path = "")
            .withBody(invalidRequestJson_data)
        )
        status(result) mustEqual BAD_REQUEST
      }
    }

    "submitUktr() called with an invalid nil return request" should {
      "return 400 BAD_REQUEST response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "", path = "")
            .withBody(invalidRequestJson_nilReturn)
        )
        status(result) mustEqual BAD_REQUEST
      }
    }

    "submitUktr() called with an invalid json request" should {
      "return 400 BAD_REQUEST response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "", path = "")
            .withBody(invalidRequest_Json)
        )
        status(result) mustEqual BAD_REQUEST
      }
    }

    "submitUktr() called with an non-json request" should {
      "return 400 BAD_REQUEST response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "", path = "")
            .withBody(invalidRequest_wrongType)
        )
        status(result) mustEqual BAD_REQUEST
      }
    }

    "submitUktr() called with request that only contains a valid return type" should {
      "return 400 BAD_REQUEST response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "", path = "")
            .withBody(invalidRequest_nilReturn_onlyContainsLiabilities)
        )
        status(result) mustEqual BAD_REQUEST
      }
    }

    "submitUktr() called with request that only contains an invalid return type" should {
      "return 400 BAD_REQUEST response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "", path = "")
            .withBody(invalidRequest_nilReturn_onlyLiabilitiesButInvalidReturnType)
        )
        status(result) mustEqual BAD_REQUEST
      }
    }

    "submitUktr() called with request that is missing liabilities" should {
      "return 400 BAD_REQUEST response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "", path = "")
            .withBody(invalidRequest_noLiabilities)
        )
        status(result) mustEqual BAD_REQUEST
      }
    }

    "submitUktr() called with no request body" should {
      "return 400 BAD_REQUEST response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "", path = "")
            .withBody(invalidRequest_noBody)
        )
        status(result) mustEqual BAD_REQUEST
      }
    }
  }
}

object UktrSubmissionControllerSpec {
  val validRequestJson_data: AnyContentAsJson =
    AnyContentAsJson(Json.parse("""{
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
      |}""".stripMargin))

  val validRequestJson_nilReturn: AnyContentAsJson =
    AnyContentAsJson(Json.parse("""{
      |  "accountingPeriodFrom": "2024-08-14",
      |  "accountingPeriodTo": "2024-09-14",
      |  "obligationMTT": true,
      |  "electionUKGAAP": true,
      |  "liabilities": {
      |    "returnType": "NIL_RETURN"
      |  }
      |}
      |""".stripMargin))

  val invalidRequestJson_data: AnyContentAsJson =
    AnyContentAsJson(Json.parse("""{
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
      |}""".stripMargin))

  val invalidRequestJson_nilReturn: AnyContentAsJson =
    AnyContentAsJson(Json.parse("""{
      |  "accountingPeriodFrom": "2024-08-14",
      |  "accountingPeriodTo": "2024-12-14",
      |  "obligationMTT": true,
      |  "liabilities": {
      |    "returnType": "INVALID"
      |  }
      |}""".stripMargin))

  val invalidRequest_Json: AnyContentAsJson =
    AnyContentAsJson(Json.parse("""{
      |  "badRequest": ""
      |}""".stripMargin))

  val invalidRequest_wrongType: AnyContent =
    AnyContent("This is not Json.")
  val invalidRequest_nilReturn_onlyContainsLiabilities: AnyContentAsJson =
    AnyContentAsJson(Json.parse("""{
      |  "liabilities": {
      |    "returnType": "NIL_RETURN"
      |  }
      |}
      |""".stripMargin))
  val invalidRequest_nilReturn_onlyLiabilitiesButInvalidReturnType: AnyContentAsJson =
    AnyContentAsJson(Json.parse("""{
      |  "liabilities": {
      |    "returnType": "INVALID"
      |  }
      |}
      |""".stripMargin))
  val invalidRequest_noLiabilities: AnyContentAsJson =
    AnyContentAsJson(Json.parse("""{
        |  "accountingPeriodFrom": "2024-08-14",
        |  "accountingPeriodTo": "2024-12-14",
        |  "obligationMTT": true
        |}""".stripMargin))
  val invalidRequest_noBody: Unit = ()
}
