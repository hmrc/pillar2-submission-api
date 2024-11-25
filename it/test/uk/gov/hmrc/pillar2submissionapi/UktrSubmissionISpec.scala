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

package uk.gov.hmrc.pillar2submissionapi

import org.scalatest.OptionValues.convertOptionToValuable
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpVerbs.POST
import uk.gov.hmrc.pillar2submissionapi.UktrSubmissionISpec._
import uk.gov.hmrc.pillar2submissionapi.base.IntegrationSpecBase
import uk.gov.hmrc.pillar2submissionapi.controllers.routes

class UktrSubmissionISpec extends IntegrationSpecBase {

  "UKTR Submission" when {
    "Creating a new UKTR submission (POST)" that {
      "has valid submission data" should {
        val request = FakeRequest(POST, routes.UktrSubmissionController.submitUktr.url)
          .withBody[JsValue](correctedValidRequestJson)

        "return a 201 CREATED response" in {
          val application = applicationBuilder().build()
          running(application) {
            val result = route(application, request).value
            status(result) mustEqual CREATED
          }
        }
      }
      "has valid nil-return submission data" should {
        val request = FakeRequest(POST, routes.UktrSubmissionController.submitUktr.url)
          .withBody[JsValue](validRequestNilReturnJson)

        "return a 201 CREATED response" in {
          val application = applicationBuilder().build()
          running(application) {
            val result = route(application, request).value
            status(result) mustEqual CREATED
          }
        }
      }
      "has an invalid request body" should {
        val request = FakeRequest(POST, routes.UktrSubmissionController.submitUktr.url)
          .withBody[JsValue](invalidRequestJson)

        "return a 400 BAD_REQUEST response" in {
          val application = applicationBuilder().build()
          running(application) {
            val result = route(application, request).value
            status(result) mustEqual BAD_REQUEST
          }
        }
      }
      "has an empty request body" should {
        val request = FakeRequest(POST, routes.UktrSubmissionController.submitUktr.url)
          .withBody(JsObject.empty)

        "return a 400 BAD_REQUEST response" in {
          val application = applicationBuilder().build()
          running(application) {
            val result = route(application, request).value
            status(result) mustEqual BAD_REQUEST
          }
        }
      }
      "has no request body" should {
        val request = FakeRequest(POST, routes.UktrSubmissionController.submitUktr.url)

        "return a 400 BAD_REQUEST response" in {
          val application = applicationBuilder().build()
          running(application) {
            val result = route(application, request).value
            status(result) mustEqual BAD_REQUEST
          }
        }
      }

      "has a valid request body containing duplicates fields and additional fields" should {
        val request = FakeRequest(POST, routes.UktrSubmissionController.submitUktr.url)
          .withJsonBody(validRequestJson_duplicateFieldsAndAdditionalFields)

        "return a 201 CREATED response" in {
          val application = applicationBuilder().build()
          running(application) {
            val result = route(application, request).value
            status(result) mustEqual CREATED
          }
        }
      }
    }

    "Amend a UKTR submission (PUT)" that {
      "has a valid request body" should {
        "return a 200" in {
          // Not yet implemented
          true
        }
      }
    }
  }
}

object UktrSubmissionISpec {

  val correctedValidRequestJson: JsValue =
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
                 |}
                 |""".stripMargin)

  val validRequestJson: JsValue =
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

  val validRequestNilReturnJson: JsValue =
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

  val invalidRequestJson: JsValue =
    Json.parse("""{
        |  "badRequest": ""
        |}""".stripMargin)

  val validRequestJson_duplicateFieldsAndAdditionalFields: JsValue =
    Json.parse(
      """{
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
        |}""".stripMargin
    )
}
