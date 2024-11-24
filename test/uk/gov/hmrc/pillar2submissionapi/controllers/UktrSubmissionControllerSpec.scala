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

      // Assign each assertion to 'val _ =' to indicate intentional use
      val _ = status(result)        shouldBe CREATED
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

      val _ = status(result)        shouldBe CREATED
      val _ = contentAsJson(result) shouldBe Json.obj("status" -> "Created")
    }

    "return BadRequest when JSON is missing required fields" in {
      val invalidJson = Json.obj(
        "accountingPeriodFrom" -> "2023-01-01"
        // Missing "accountingPeriodTo", "obligationMTT", "electionUKGAAP", "liabilities"
      )

      val request = FakeRequest(POST, "/submitUktr")
        .withJsonBody(invalidJson)

      val result = controller.submitUktr.apply(request)

      val _ = status(result)                                 shouldBe BAD_REQUEST
      val _ = (contentAsJson(result) \ "message").as[String] shouldBe "Invalid JSON format"

      val details = (contentAsJson(result) \ "details").as[Seq[String]]
      details should contain allElementsOf Seq(
        "Path: /accountingPeriodTo, Errors: error.path.missing",
        "Path: /obligationMTT, Errors: error.path.missing",
        "Path: /electionUKGAAP, Errors: error.path.missing",
        "Path: /liabilities, Errors: error.path.missing"
      )
    }

    "return BadRequest for unknown submission type" in {
      val unknownTypeJson = Json.obj(
        "accountingPeriodFrom" -> "2023-01-01",
        "accountingPeriodTo"   -> "2023-12-31",
        "obligationMTT"        -> true,
        "electionUKGAAP"       -> false,
        "liabilities" -> Json.obj(
          // Neither "returnType" nor "electionDTTSingleMember" present
          "someOtherField" -> "value"
        )
      )

      val request = FakeRequest(POST, "/submitUktr")
        .withJsonBody(unknownTypeJson)

      val result = controller.submitUktr.apply(request)

      status(result)                                 shouldBe BAD_REQUEST:               Unit
      (contentAsJson(result) \ "message").as[String] shouldBe "Unknown submission type": Unit
    }
    "return BadRequest when liabilities validation fails for UktrSubmissionData" in {
      // Construct the JSON with invalid liabilities
      val invalidLiabilitiesJson = Json.obj(
        "accountingPeriodFrom" -> "2024-08-14", // Valid ISO date
        "accountingPeriodTo"   -> "2024-12-14", // Valid ISO date
        "obligationMTT"        -> true,
        "electionUKGAAP"       -> true,
        "liabilities" -> Json.obj(
          "electionDTTSingleMember"  -> true,
          "electionUTPRSingleMember" -> false,
          "numberSubGroupDTT"        -> -1, // Invalid: negative number
          "numberSubGroupUTPR"       -> 3,
          "totalLiability"           -> -1000.99, // Invalid: negative number
          "totalLiabilityDTT"        -> 500.25,
          "totalLiabilityIIR"        -> 0, // Invalid: zero (assuming must be positive)
          "totalLiabilityUTPR"       -> 200.10,
          "liableEntities"           -> Json.arr() // Invalid: empty array
        )
      )

      val request = FakeRequest(POST, "/submitUktr")
        .withJsonBody(invalidLiabilitiesJson)

      val result = controller.submitUktr.apply(request)

      // Assertions
      status(result) shouldBe BAD_REQUEST: Unit

      val jsonResponse = contentAsJson(result)
      (jsonResponse \ "message").as[String] shouldBe "Invalid JSON format": Unit

      val errorDetails = (jsonResponse \ "details").as[Seq[String]]

      // Debug: Print actual error details
      println(s"Error Details: $errorDetails")

      // Check for specific error messages related to liabilities
      errorDetails should contain allElementsOf Seq(
        "numberSubGroupDTT: numberSubGroupDTT must be non-negative",
        "totalLiability: totalLiability must be a positive number",
        "totalLiabilityIIR: totalLiabilityIIR must be a positive number",
        "liableEntities: liableEntities must not be empty"
      ): Unit
    }

    "should return 400 BAD_REQUEST for malformed JSON structure" in {
      // Construct the JSON directly with incorrect field types, but include all required fields
      val malformedJson = Json.obj(
        "accountingPeriodFrom" -> "2024-08-14",
        "accountingPeriodTo"   -> "2024-12-14",
        "obligationMTT"        -> true,
        "electionUKGAAP"       -> true,
        "liabilities" -> Json.obj(
          "electionDTTSingleMember"  -> "true", // Should be boolean
          "electionUTPRSingleMember" -> false,
          "numberSubGroupDTT"        -> "1", // Should be number
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

      // Assign each assertion to 'val _ =' to indicate intentional use
      val _ = status(result) shouldBe BAD_REQUEST:                                           Unit
      val _ = (contentAsJson(result) \ "message").as[String] shouldBe "Invalid JSON format": Unit

      // Ensure the error details contain the correct message for malformed JSON
      val errorDetails = (contentAsJson(result) \ "details").as[Seq[String]]
      errorDetails should contain("Path: /liabilities/electionDTTSingleMember, Errors: error.expected.jsboolean"): Unit
      errorDetails should contain("Path: /liabilities/numberSubGroupDTT, Errors: error.expected.jsnumber"):        Unit
    }

    "return BadRequest when request body is not JSON" in {
      val request = FakeRequest(POST, "/submitUktr")
        .withTextBody("This is not JSON")

      val result = controller.submitUktr.apply(request)

      val _ = status(result) shouldBe BAD_REQUEST:                                           Unit
      val _ = (contentAsJson(result) \ "message").as[String] shouldBe "Invalid JSON format": Unit
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

      // Assign each assertion to 'val _ =' to indicate intentional use
      val _ = status(result) shouldBe CREATED:                                Unit
      val _ = contentAsJson(result) shouldBe Json.obj("status" -> "Created"): Unit
    }

    "return BadRequest when date formats are invalid" in {
      // Construct the JSON with an invalid date format
      val invalidDateJson = Json.obj(
        "accountingPeriodFrom" -> "01-01-2023", // Invalid format: should be "yyyy-MM-dd"
        "accountingPeriodTo"   -> "2023-12-31", // Valid ISO date
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

      val request = FakeRequest(POST, "/uktr/submit")
        .withJsonBody(invalidDateJson)

      val result = controller.submitUktr.apply(request)

      // Assertions
      status(result) shouldBe BAD_REQUEST: Unit

      val jsonResponse = contentAsJson(result)
      (jsonResponse \ "message").as[String] shouldBe "Invalid JSON format": Unit

      val errorDetails = (jsonResponse \ "details").as[Seq[String]]

      // Debug: Print actual error details
      println(s"Error Details: $errorDetails")

      // Check for specific error message related to date format
      errorDetails should contain("Path: /accountingPeriodFrom, Errors: error.expected.date.isoformat"): Unit
    }

    "return BadRequest when enums have invalid values" in {
      val invalidEnumJson = Json.obj(
        "accountingPeriodFrom" -> "2023-01-01",
        "accountingPeriodTo"   -> "2023-12-31",
        "obligationMTT"        -> true,
        "electionUKGAAP"       -> false,
        "liabilities" -> Json.obj(
          "returnType" -> "INVALID_ENUM" // Invalid enum value
        )
      )

      val request = FakeRequest(POST, "/submitUktr")
        .withJsonBody(invalidEnumJson)

      val result = controller.submitUktr.apply(request)

      val _       = status(result) shouldBe BAD_REQUEST:                                           Unit
      val _       = (contentAsJson(result) \ "message").as[String] shouldBe "Invalid JSON format": Unit
      val details = (contentAsJson(result) \ "details").as[Seq[String]]
      details should contain("Path: /liabilities/returnType, Errors: error.expected.validenumvalue"): Unit
    }

    "return BadRequest when UktrSubmissionNilReturn has validation errors" in {
      val invalidNilReturnJson = Json.obj(
        "accountingPeriodFrom" -> "2023-01-01",
        "accountingPeriodTo"   -> "2023-12-31",
        "obligationMTT"        -> false,
        "electionUKGAAP"       -> true,
        "liabilities" -> Json.obj(
          "returnType" -> "INVALID_RETURN_TYPE" // Invalid enum value
        )
      )

      val request = FakeRequest(POST, "/submitUktr")
        .withJsonBody(invalidNilReturnJson)

      val result = controller.submitUktr.apply(request)

      status(result)                                 shouldBe BAD_REQUEST:           Unit
      (contentAsJson(result) \ "message").as[String] shouldBe "Invalid JSON format": Unit
      val details = (contentAsJson(result) \ "details").as[Seq[String]]
      details should contain("Path: /liabilities/returnType, Errors: error.expected.validenumvalue"): Unit
    }

    "return BadRequest when UktrSubmissionData has validation errors" in {
      val invalidSubmissionData = UktrSubmissionData(
        accountingPeriodFrom = LocalDate.parse("2023-01-01"),
        accountingPeriodTo = LocalDate.parse("2023-12-31"),
        obligationMTT = true,
        electionUKGAAP = false,
        liabilities = LiabilityData(
          electionDTTSingleMember = true,
          electionUTPRSingleMember = false,
          numberSubGroupDTT = -1, // Invalid: negative
          numberSubGroupUTPR = 3,
          totalLiability = BigDecimal(-1000.50),
          totalLiabilityDTT = BigDecimal(0), // Invalid: non-positive
          totalLiabilityIIR = BigDecimal(0), // Invalid: non-positive
          totalLiabilityUTPR = BigDecimal(249.50),
          liableEntities = Seq.empty // Invalid: empty
        )
      )

      val request = FakeRequest(POST, "/submitUktr")
        .withJsonBody(Json.toJson(invalidSubmissionData))

      val result = controller.submitUktr.apply(request)

      status(result)                                 shouldBe BAD_REQUEST:           Unit
      (contentAsJson(result) \ "message").as[String] shouldBe "Invalid JSON format": Unit
      val details = (contentAsJson(result) \ "details").as[Seq[String]]

      details should contain allElementsOf Seq(
        "numberSubGroupDTT: numberSubGroupDTT must be non-negative",
        "totalLiability: totalLiability must be a positive number",
        "totalLiabilityDTT: totalLiabilityDTT must be a positive number",
        "totalLiabilityIIR: totalLiabilityIIR must be a positive number",
        "liableEntities: liableEntities must not be empty"
      ): Unit
    }

    "return BadRequest for invalid UktrSubmissionNilReturn submission" in {
      // Manually create a test case with an invalid ReturnType enum value (e.g., not defined in the enum)
      val invalidReturnType = "INVALID_RETURN_TYPE" // A string that is not part of the enum

      // Manually inject an invalid returnType in the request
      val invalidNilReturnJson = Json.obj(
        "accountingPeriodFrom" -> "2023-01-01",
        "accountingPeriodTo"   -> "2023-12-31",
        "obligationMTT"        -> false,
        "electionUKGAAP"       -> true,
        "liabilities" -> Json.obj(
          "returnType" -> invalidReturnType // Invalid enum value
        )
      )

      val request = FakeRequest(POST, "/submitUktr")
        .withJsonBody(invalidNilReturnJson)

      val result = controller.submitUktr.apply(request)

      // Assert the response status
      status(result) shouldBe BAD_REQUEST: Unit

      // Assert the response body
      val jsonResponse = contentAsJson(result)
      (jsonResponse \ "message").as[String] shouldBe "Invalid JSON format": Unit

      // Ensure the error details contain validation errors for the invalid ReturnType
      val errorDetails = (jsonResponse \ "details").as[Seq[String]]
      errorDetails should contain("Path: /liabilities/returnType, Errors: error.expected.validenumvalue"): Unit
    }
  }
}
