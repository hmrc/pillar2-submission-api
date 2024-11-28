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

package uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json._

import java.time.LocalDate

class UktrSubmissionSpec extends AnyFreeSpec with Matchers {

  "UktrSubmission Reads" - {

    "should fail with JsError for invalid liability structure" in {
      val invalidLiabilitiesJson = Json.parse(
        """
          |{
          |  "accountingPeriodFrom": "2023-01-01",
          |  "accountingPeriodTo": "2023-12-31",
          |  "obligationMTT": true,
          |  "electionUKGAAP": false,
          |  "liabilities": {
          |    "unknownField": "unexpectedValue"
          |  }
          |}
          |""".stripMargin
      )

      val result = invalidLiabilitiesJson.validate[UktrSubmission]

      result match {
        case JsError(errors) =>
          errors should contain allElementsOf Seq(
            (JsPath \ "liabilities" \ "electionDTTSingleMember", Seq(JsonValidationError("error.path.missing"))),
            (JsPath \ "liabilities" \ "electionUTPRSingleMember", Seq(JsonValidationError("error.path.missing"))),
            (JsPath \ "liabilities" \ "numberSubGroupDTT", Seq(JsonValidationError("error.path.missing"))),
            (JsPath \ "liabilities" \ "numberSubGroupUTPR", Seq(JsonValidationError("error.path.missing"))),
            (JsPath \ "liabilities" \ "totalLiability", Seq(JsonValidationError("error.path.missing"))),
            (JsPath \ "liabilities" \ "totalLiabilityDTT", Seq(JsonValidationError("error.path.missing"))),
            (JsPath \ "liabilities" \ "totalLiabilityIIR", Seq(JsonValidationError("error.path.missing"))),
            (JsPath \ "liabilities" \ "totalLiabilityUTPR", Seq(JsonValidationError("error.path.missing"))),
            (JsPath \ "liabilities" \ "liableEntities", Seq(JsonValidationError("error.path.missing")))
          )
        case _ => fail("Expected JsError but got JsSuccess")
      }
    }

  }

  "UktrSubmission Writes" - {

    "should correctly serialize UktrSubmissionData to JSON" in {
      val liabilities = LiabilityData(
        electionDTTSingleMember = true,
        electionUTPRSingleMember = false,
        numberSubGroupDTT = 1,
        numberSubGroupUTPR = 2,
        totalLiability = BigDecimal(1000.50),
        totalLiabilityDTT = BigDecimal(500.25),
        totalLiabilityIIR = BigDecimal(300.15),
        totalLiabilityUTPR = BigDecimal(200.10),
        liableEntities = Seq(
          LiableEntity(
            ukChargeableEntityName = "Entity One",
            idType = "TypeA",
            idValue = "ID123",
            amountOwedDTT = BigDecimal(250.00),
            amountOwedIIR = BigDecimal(150.00),
            amountOwedUTPR = BigDecimal(100.00)
          )
        )
      )

      val submissionData = UktrSubmissionData(
        accountingPeriodFrom = LocalDate.of(2023, 1, 1),
        accountingPeriodTo = LocalDate.of(2023, 12, 31),
        obligationMTT = true,
        electionUKGAAP = false,
        liabilities = liabilities
      )

      val serializedJson = Json.toJson(submissionData: UktrSubmission)

      val expectedJson = Json.parse(
        s"""
           |{
           |  "accountingPeriodFrom": "2023-01-01",
           |  "accountingPeriodTo": "2023-12-31",
           |  "obligationMTT": true,
           |  "electionUKGAAP": false,
           |  "liabilities": {
           |    "electionDTTSingleMember": true,
           |    "electionUTPRSingleMember": false,
           |    "numberSubGroupDTT": 1,
           |    "numberSubGroupUTPR": 2,
           |    "totalLiability": 1000.50,
           |    "totalLiabilityDTT": 500.25,
           |    "totalLiabilityIIR": 300.15,
           |    "totalLiabilityUTPR": 200.10,
           |    "liableEntities": [
           |      {
           |        "ukChargeableEntityName": "Entity One",
           |        "idType": "TypeA",
           |        "idValue": "ID123",
           |        "amountOwedDTT": 250.00,
           |        "amountOwedIIR": 150.00,
           |        "amountOwedUTPR": 100.00
           |      }
           |    ]
           |  }
           |}
           |""".stripMargin
      )

      serializedJson shouldBe expectedJson
    }

    "should correctly serialize UktrSubmissionNilReturn to JSON" in {
      val nilReturn = UktrSubmissionNilReturn(
        accountingPeriodFrom = LocalDate.of(2023, 1, 1),
        accountingPeriodTo = LocalDate.of(2023, 12, 31),
        obligationMTT = false,
        electionUKGAAP = true,
        liabilities = LiabilityNilReturn(ReturnType.NIL_RETURN)
      )

      val serializedJson = Json.toJson(nilReturn: UktrSubmission)

      val expectedJson = Json.parse(
        s"""
           |{
           |  "accountingPeriodFrom": "2023-01-01",
           |  "accountingPeriodTo": "2023-12-31",
           |  "obligationMTT": false,
           |  "electionUKGAAP": true,
           |  "liabilities": {
           |    "returnType": "NIL_RETURN"
           |  }
           |}
           |""".stripMargin
      )

      serializedJson shouldBe expectedJson
    }
  }
}
