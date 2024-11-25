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
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions._

import java.time.LocalDate

class UktrSubmissionControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  private val controller = new UktrSubmissionController(Helpers.stubControllerComponents())

  "UktrSubmissionController#submitUktr" should {

    "return Created for a valid UktrSubmissionData submission" in {
      val validData = UktrSubmissionData(
        accountingPeriodFrom = LocalDate.parse("2023-01-01"),
        accountingPeriodTo = LocalDate.parse("2023-12-31"),
        obligationMTT = true,
        electionUKGAAP = false,
        liabilities = LiabilityData(
          electionDTTSingleMember = true,
          electionUTPRSingleMember = false,
          numberSubGroupDTT = 2,
          numberSubGroupUTPR = 3,
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
      )

      val request = FakeRequest(POST, "/submitUktr")
        .withJsonBody(Json.toJson(validData))

      val result = controller.submitUktr.apply(request)

      val _ = status(result) shouldBe CREATED: Unit
      val _ = contentAsJson(result) shouldBe Json.obj("status" -> "Created")
    }

    "return Created for a valid UktrSubmissionNilReturn submission" in {
      val validNilReturn = UktrSubmissionNilReturn(
        accountingPeriodFrom = LocalDate.parse("2023-01-01"),
        accountingPeriodTo = LocalDate.parse("2023-12-31"),
        obligationMTT = false,
        electionUKGAAP = true,
        liabilities = LiabilityNilReturn(ReturnType.NIL_RETURN)
      )

      val request = FakeRequest(POST, "/submitUktr")
        .withJsonBody(Json.toJson(validNilReturn))

      val result = controller.submitUktr.apply(request)

      val _ = status(result) shouldBe CREATED: Unit
      val _ = contentAsJson(result) shouldBe Json.obj("status" -> "Created")
    }

    "return BadRequest when JSON is missing required fields" in {
      val invalidJson = Json.obj(
        "accountingPeriodFrom" -> "2023-01-01"
      )

      val request = FakeRequest(POST, "/submitUktr")
        .withJsonBody(invalidJson)

      val result = controller.submitUktr.apply(request)

      status(result)                                 shouldBe BAD_REQUEST:           Unit
      (contentAsJson(result) \ "message").as[String] shouldBe "Invalid JSON format": Unit

      val details = (contentAsJson(result) \ "details").as[Seq[String]]
      details should contain allElementsOf Seq(
        "Path: /accountingPeriodTo, Errors: error.path.missing",
        "Path: /obligationMTT, Errors: error.path.missing",
        "Path: /electionUKGAAP, Errors: error.path.missing",
        "Path: /liabilities, Errors: error.path.missing"
      ): Unit
    }

    "return BadRequest when liabilities validation fails for UktrSubmissionData" in {

      val invalidLiabilitiesJson = Json.obj(
        "accountingPeriodFrom" -> "2024-08-14",
        "accountingPeriodTo"   -> "2024-12-14",
        "obligationMTT"        -> true,
        "electionUKGAAP"       -> true,
        "liabilities" -> Json.obj(
          "electionDTTSingleMember"  -> true,
          "electionUTPRSingleMember" -> false,
          "numberSubGroupDTT"        -> -1,
          "numberSubGroupUTPR"       -> 3,
          "totalLiability"           -> -1000.99,
          "totalLiabilityDTT"        -> 500.25,
          "totalLiabilityIIR"        -> 0,
          "totalLiabilityUTPR"       -> 200.10,
          "liableEntities"           -> Json.arr()
        )
      )

      val request = FakeRequest(POST, "/submitUktr")
        .withJsonBody(invalidLiabilitiesJson)

      val result = controller.submitUktr.apply(request)

      status(result) shouldBe BAD_REQUEST: Unit

      val jsonResponse = contentAsJson(result)
      (jsonResponse \ "message").as[String] shouldBe "Invalid JSON format": Unit

      val errorDetails = (jsonResponse \ "details").as[Seq[String]]

      println(s"Error Details: $errorDetails")

      errorDetails should contain allElementsOf Seq(
        "numberSubGroupDTT: numberSubGroupDTT must be non-negative",
        "totalLiability: totalLiability must be a positive number",
        "totalLiabilityIIR: totalLiabilityIIR must be a positive number",
        "liableEntities: liableEntities must not be empty"
      ): Unit
    }

    "should return 400 BAD_REQUEST for malformed JSON structure" in {

      val malformedJson = Json.obj(
        "accountingPeriodFrom" -> "2024-08-14",
        "accountingPeriodTo"   -> "2024-12-14",
        "obligationMTT"        -> true,
        "electionUKGAAP"       -> true,
        "liabilities" -> Json.obj(
          "electionDTTSingleMember"  -> "true",
          "electionUTPRSingleMember" -> false,
          "numberSubGroupDTT"        -> "1",
          "numberSubGroupUTPR"       -> 3,
          "totalLiability"           -> 10000.99,
          "totalLiabilityDTT"        -> 500.25,
          "totalLiabilityIIR"        -> 300.15,
          "totalLiabilityUTPR"       -> 200.10,
          "liableEntities" -> Json.arr(
            Json.obj(
              "ukChargeableEntityName" -> "Entity One",
              "idType"                 -> "TypeA",
              "idValue"                -> "ID123",
              "amountOwedDTT"          -> 250.00,
              "amountOwedIIR"          -> 150.00,
              "amountOwedUTPR"         -> 100.00
            )
          )
        )
      )

      val request = FakeRequest(POST, "/submitUktr").withJsonBody(malformedJson)
      val result  = controller.submitUktr.apply(request)

      status(result)                                 shouldBe BAD_REQUEST:           Unit
      (contentAsJson(result) \ "message").as[String] shouldBe "Invalid JSON format": Unit

      val errorDetails = (contentAsJson(result) \ "details").as[Seq[String]]
      errorDetails should contain("Path: /liabilities/electionDTTSingleMember, Errors: error.expected.jsboolean"): Unit
      errorDetails should contain("Path: /liabilities/numberSubGroupDTT, Errors: error.expected.jsnumber"):        Unit
    }

    "return BadRequest when request body is not JSON" in {
      val request = FakeRequest(POST, "/submitUktr")
        .withTextBody("This is not JSON")

      val result = controller.submitUktr.apply(request)

      status(result)                                 shouldBe BAD_REQUEST:           Unit
      (contentAsJson(result) \ "message").as[String] shouldBe "Invalid JSON format": Unit
    }

    "return BadRequest when JSON has unknown fields" in {
      val jsonWithUnknownFields = Json.obj(
        "accountingPeriodFrom" -> "2023-01-01",
        "accountingPeriodTo"   -> "2023-12-31",
        "obligationMTT"        -> true,
        "electionUKGAAP"       -> false,
        "liabilities" -> Json.obj(
          "electionDTTSingleMember"  -> true,
          "electionUTPRSingleMember" -> false,
          "numberSubGroupDTT"        -> 2,
          "numberSubGroupUTPR"       -> 3,
          "totalLiability"           -> 1000.50,
          "totalLiabilityDTT"        -> 500.25,
          "totalLiabilityIIR"        -> 300.15,
          "totalLiabilityUTPR"       -> 200.10,
          "liableEntities" -> Json.arr(
            Json.obj(
              "ukChargeableEntityName" -> "Entity One",
              "idType"                 -> "TypeA",
              "idValue"                -> "ID123",
              "amountOwedDTT"          -> 250.00,
              "amountOwedIIR"          -> 150.00,
              "amountOwedUTPR"         -> 100.00,
              "unknownField"           -> "unexpected"
            )
          ),
          "unknownLiabilityField" -> "unexpected"
        ),
        "extraTopLevelField" -> "unexpected"
      )

      val request = FakeRequest(POST, "/submitUktr")
        .withJsonBody(jsonWithUnknownFields)

      val result = controller.submitUktr.apply(request)

      status(result)        shouldBe CREATED:                         Unit
      contentAsJson(result) shouldBe Json.obj("status" -> "Created"): Unit
    }
    "return BadRequest when date formats are invalid" in {
      val invalidDateJson = Json.obj(
        "accountingPeriodFrom" -> "01-01-2023", // Invalid date format
        "accountingPeriodTo"   -> "2023-12-31",
        "obligationMTT"        -> true,
        "electionUKGAAP"       -> false,
        "liabilities" -> Json.obj(
          "electionDTTSingleMember"  -> true,
          "electionUTPRSingleMember" -> false,
          "numberSubGroupDTT"        -> 2,
          "numberSubGroupUTPR"       -> 3,
          "totalLiability"           -> 1000.50,
          "totalLiabilityDTT"        -> 500.25,
          "totalLiabilityIIR"        -> 300.15,
          "totalLiabilityUTPR"       -> 200.10,
          "liableEntities" -> Json.arr(
            Json.obj(
              "ukChargeableEntityName" -> "Entity One",
              "idType"                 -> "TypeA",
              "idValue"                -> "ID123",
              "amountOwedDTT"          -> 250.00,
              "amountOwedIIR"          -> 150.00,
              "amountOwedUTPR"         -> 100.00
            )
          )
        )
      )

      val request = FakeRequest(POST, "/submitUktr")
        .withJsonBody(invalidDateJson)

      val result = controller.submitUktr.apply(request)

      status(result)                                 shouldBe BAD_REQUEST:           Unit
      (contentAsJson(result) \ "message").as[String] shouldBe "Invalid JSON format": Unit

      val errorDetails = (contentAsJson(result) \ "details").as[Seq[String]]
      errorDetails should contain("Path: /accountingPeriodFrom, Errors: error.expected.date.isoformat"): Unit
    }

    "return BadRequest for unknown submission type" in {
      val unknownTypeJson = Json.obj(
        "accountingPeriodFrom" -> "2023-01-01",
        "accountingPeriodTo"   -> "2023-12-31",
        "obligationMTT"        -> true,
        "electionUKGAAP"       -> false,
        "liabilities"          -> Json.obj("returnType" -> "UNKNOWN_TYPE")
      )

      val request = FakeRequest(POST, "/submitUktr").withJsonBody(unknownTypeJson)
      val result  = controller.submitUktr.apply(request)

      status(result)                                 shouldBe BAD_REQUEST:           Unit
      (contentAsJson(result) \ "message").as[String] shouldBe "Invalid JSON format": Unit

      val details = (contentAsJson(result) \ "details").as[Seq[String]]
      details should contain("Path: /liabilities/returnType, Errors: Unknown submission type: UNKNOWN_TYPE"): Unit
    }

    "return BadRequest when enums have invalid values" in {
      val invalidEnumJson = Json.obj(
        "accountingPeriodFrom" -> "2023-01-01",
        "accountingPeriodTo"   -> "2023-12-31",
        "obligationMTT"        -> true,
        "electionUKGAAP"       -> false,
        "liabilities"          -> Json.obj("returnType" -> "INVALID_ENUM")
      )

      val request = FakeRequest(POST, "/submitUktr").withJsonBody(invalidEnumJson)
      val result  = controller.submitUktr.apply(request)

      status(result)                                 shouldBe BAD_REQUEST:           Unit
      (contentAsJson(result) \ "message").as[String] shouldBe "Invalid JSON format": Unit

      val details = (contentAsJson(result) \ "details").as[Seq[String]]
      details should contain("Path: /liabilities/returnType, Errors: Unknown submission type: INVALID_ENUM"): Unit
    }

    "return BadRequest when UktrSubmissionNilReturn has different validation errors" in {
      val anotherInvalidNilReturnJson = Json.obj(
        "accountingPeriodFrom" -> "2023-01-01",
        "accountingPeriodTo"   -> "2023-12-31",
        "obligationMTT"        -> false,
        "electionUKGAAP"       -> true,
        "liabilities" -> Json.obj(
          "returnType" -> "ANOTHER_INVALID_RETURN_TYPE"
        )
      )

      val request = FakeRequest(POST, "/submitUktr").withJsonBody(anotherInvalidNilReturnJson)
      val result  = controller.submitUktr.apply(request)

      status(result)                                 shouldBe BAD_REQUEST:           Unit
      (contentAsJson(result) \ "message").as[String] shouldBe "Invalid JSON format": Unit

      val details = (contentAsJson(result) \ "details").as[Seq[String]]
      details should contain("Path: /liabilities/returnType, Errors: Unknown submission type: ANOTHER_INVALID_RETURN_TYPE"): Unit
    }

    "return BadRequest when UktrSubmissionData has validation errors" in {
      val invalidSubmissionDataJson = Json.obj(
        "accountingPeriodFrom" -> "2023-01-01",
        "accountingPeriodTo"   -> "2023-12-31",
        "obligationMTT"        -> true,
        "electionUKGAAP"       -> false,
        "liabilities" -> Json.obj(
          "electionDTTSingleMember"  -> true,
          "electionUTPRSingleMember" -> false,
          "numberSubGroupDTT"        -> -1, // Invalid: negative
          "numberSubGroupUTPR"       -> 3, // Valid
          "totalLiability"           -> -1000.50, // Invalid: negative
          "totalLiabilityDTT"        -> 500.25, // Valid
          "totalLiabilityIIR"        -> 0, // Invalid: zero
          "totalLiabilityUTPR"       -> 200.10, // Valid
          "liableEntities"           -> Json.arr() // Invalid: empty array
        )
      )

      val request = FakeRequest(POST, "/submitUktr").withJsonBody(invalidSubmissionDataJson)
      val result  = controller.submitUktr.apply(request)

      status(result)                                 shouldBe BAD_REQUEST:           Unit
      (contentAsJson(result) \ "message").as[String] shouldBe "Invalid JSON format": Unit

      val details = (contentAsJson(result) \ "details").as[Seq[String]]
      details should contain allElementsOf Seq(
        "numberSubGroupDTT: numberSubGroupDTT must be non-negative",
        "totalLiability: totalLiability must be a positive number",
        "totalLiabilityIIR: totalLiabilityIIR must be a positive number",
        "liableEntities: liableEntities must not be empty"
      ): Unit
    }

    "return BadRequest when UktrSubmissionNilReturn has validation errors" in {
      val invalidNilReturnJson = Json.obj(
        "accountingPeriodFrom" -> "2023-01-01",
        "accountingPeriodTo"   -> "2023-12-31",
        "obligationMTT"        -> false,
        "electionUKGAAP"       -> true,
        "liabilities" -> Json.obj(
          "returnType" -> "NIL_RETURN"
        )
      )

      val request = FakeRequest(POST, "/submitUktr").withJsonBody(invalidNilReturnJson)
      val result  = controller.submitUktr.apply(request)

      status(result)                                 shouldBe BAD_REQUEST:           Unit
      (contentAsJson(result) \ "message").as[String] shouldBe "Invalid JSON format": Unit

      val details = (contentAsJson(result) \ "details").as[Seq[String]]
      details should contain("Path: /liabilities/returnType, Errors: returnType has an invalid value"): Unit
    }

  }
}
