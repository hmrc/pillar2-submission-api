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
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, AnyContentAsJson}
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import uk.gov.hmrc.pillar2submissionapi.controllers.UktrSubmissionControllerSpec._

class UktrSubmissionControllerSpec extends ControllerBaseSpec {

  val uktrSubmissionController: UktrSubmissionController = new UktrSubmissionController(cc)

  "UktrSubmissionController" when {
    "submitUktr() called with valid request" should {
      "return 200 OK response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "", path = "")
            .withBody(validRequestJson_data)
        )
        status(result) mustEqual OK
      }
    }

    "submitUktr() called with valid nil return request" should {
      "return 200 OK response" in {
        val result = uktrSubmissionController.submitUktr()(
          FakeRequest(method = "", path = "")
            .withBody(validRequestJson_nilReturn)
        )
        status(result) mustEqual OK
      }
    }

    "submitUktr() called with invalid requests" should {
      "return 400 BAD_REQUEST response" in {
        val requestTemplate = FakeRequest(method = "", path = "")
        val testCases = Table(
          ("input", "expectedResponse"),
          (requestTemplate.withBody(invalidRequestJson_nilReturn), BAD_REQUEST),
          (requestTemplate.withBody(invalidRequestJson_data), BAD_REQUEST),
          (requestTemplate.withBody(invalidRequest_Json), BAD_REQUEST),
          (requestTemplate.withBody(invalidRequest_wrongType), BAD_REQUEST),
          (requestTemplate.withBody(invalidRequest_noBody), BAD_REQUEST)
        )

        forAll(testCases) { (input: FakeRequest[Any], expectedResponse: Int) =>
          val result = uktrSubmissionController.submitUktr()(input)
          status(result) mustEqual expectedResponse
        }
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

  val validRequestJson_nilReturn: AnyContentAsJson = AnyContentAsJson(Json.parse("""{
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

  val invalidRequest_wrongType: AnyContent = AnyContent("This is not Json.")
  val invalidRequest_noBody:    Unit       = ()
}
