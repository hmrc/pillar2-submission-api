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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}

class UktrSubmissionControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  private val controller = new UktrSubmissionController(Helpers.stubControllerComponents())

  "UktrSubmissionController" should {

    "return 201 CREATED for valid UktrSubmissionData" in {
      val validRequestJson = Json.parse(
        """{
          |  "accountingPeriodFrom": "2024-08-14",
          |  "accountingPeriodTo": "2024-12-14",
          |  "obligationMTT": true,
          |  "electionUKGAAP": true,
          |  "liabilities": {
          |    "electionDTTSingleMember": true,
          |    "electionUTPRSingleMember": true,
          |    "numberSubGroupDTT": 1,
          |    "numberSubGroupUTPR": 2,
          |    "totalLiability": 10000.99,
          |    "totalLiabilityDTT": 5000.50,
          |    "totalLiabilityIIR": 4000.00,
          |    "totalLiabilityUTPR": 2000.75,
          |    "liableEntities": [
          |      {
          |        "ukChargeableEntityName": "Entity 1",
          |        "idType": "CRN",
          |        "idValue": "12345678",
          |        "amountOwedDTT": 1500.25,
          |        "amountOwedIIR": 1200.75,
          |        "amountOwedUTPR": 800.00
          |      }
          |    ]
          |  }
          |}""".stripMargin
      )

      val request = FakeRequest(POST, "/uktr/submit").withJsonBody(validRequestJson)
      val result  = controller.submitUktr()(request)

      status(result)                                shouldBe CREATED:   Unit
      (contentAsJson(result) \ "status").as[String] shouldBe "Created": Unit
    }

    "return 201 CREATED for valid UktrSubmissionNilReturn" in {
      val validNilReturnJson = Json.parse(
        """{
          |  "accountingPeriodFrom": "2024-08-14",
          |  "accountingPeriodTo": "2024-09-14",
          |  "obligationMTT": true,
          |  "electionUKGAAP": true,
          |  "liabilities": {
          |    "returnType": "NIL_RETURN"
          |  }
          |}""".stripMargin
      )

      val request = FakeRequest(POST, "/uktr/submit").withJsonBody(validNilReturnJson)
      val result  = controller.submitUktr()(request)

      // Explicitly discard assertion results
      (status(result)                                shouldBe CREATED):   Unit
      ((contentAsJson(result) \ "status").as[String] shouldBe "Created"): Unit
    }

    "return 400 BAD_REQUEST for invalid liabilities in UktrSubmissionData" in {
      val invalidRequestJson = Json.parse(
        """{
          |  "accountingPeriodFrom": "2024-08-14",
          |  "accountingPeriodTo": "2024-12-14",
          |  "obligationMTT": true,
          |  "electionUKGAAP": true,
          |  "liabilities": {
          |    "electionDTTSingleMember": true,
          |    "electionUTPRSingleMember": true,
          |    "numberSubGroupDTT": -1,
          |    "numberSubGroupUTPR": 2,
          |    "totalLiability": 10000.99,
          |    "totalLiabilityDTT": 5000.50,
          |    "totalLiabilityIIR": 4000.00,
          |    "totalLiabilityUTPR": 2000.75,
          |    "liableEntities": []
          |  }
          |}""".stripMargin
      )

      val request = FakeRequest(POST, "/uktr/submit").withJsonBody(invalidRequestJson)
      val result  = controller.submitUktr()(request)

      status(result)                                    shouldBe BAD_REQUEST:                                                          Unit
      (contentAsJson(result) \ "message").as[String]    shouldBe "Invalid JSON format":                                                Unit
      (contentAsJson(result) \ "details").as[Seq[String]] should contain("numberSubGroupDTT: numberSubGroupDTT must be non-negative"): Unit
    }

    "return 400 BAD_REQUEST for missing fields in UktrSubmissionData" in {
      val missingFieldsJson = Json.parse(
        """{
          |  "accountingPeriodFrom": "2024-08-14",
          |  "obligationMTT": true
          |}""".stripMargin
      )

      val request = FakeRequest(POST, "/uktr/submit").withJsonBody(missingFieldsJson)
      val result  = controller.submitUktr()(request)

      status(result)                                 shouldBe BAD_REQUEST:           Unit
      (contentAsJson(result) \ "message").as[String] shouldBe "Invalid JSON format": Unit
    }

    "return 400 BAD_REQUEST for unknown submission type" in {
      val unknownSubmissionTypeJson = Json.parse(
        """{
          |  "accountingPeriodFrom": "2024-08-14",
          |  "accountingPeriodTo": "2024-12-14",
          |  "obligationMTT": true,
          |  "electionUKGAAP": true,
          |  "liabilities": {
          |    "unexpectedField": "unexpectedValue"
          |  }
          |}""".stripMargin
      )

      val request = FakeRequest(POST, "/uktr/submit").withJsonBody(unknownSubmissionTypeJson)
      val result  = controller.submitUktr()(request)

      status(result)                                 shouldBe BAD_REQUEST:           Unit
      (contentAsJson(result) \ "message").as[String] shouldBe "Invalid JSON format": Unit
    }
  }
}
