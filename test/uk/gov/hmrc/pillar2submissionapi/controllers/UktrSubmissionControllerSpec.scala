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
import cats.data.{NonEmptyChain, Validated, ValidatedNec}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.mvc.Results._
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.pillar2submissionapi.errorhandling.ResponseHandler
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions._
import uk.gov.hmrc.pillar2submissionapi.validation.{LiabilityDataValidator, LiabilityNilReturnValidator, ValidationError}

class UktrSubmissionControllerSpec extends AnyWordSpec with Matchers with MockitoSugar {

  private val mockLiabilityDataValidator      = mock[LiabilityDataValidator]
  private val mockLiabilityNilReturnValidator = mock[LiabilityNilReturnValidator]
  private val mockResponseHandler             = mock[ResponseHandler]

  // Mock setup
  when(mockLiabilityDataValidator.validate(any[LiabilityData]))
    .thenAnswer { invocation: InvocationOnMock =>
      Validated.Valid(invocation.getArgument(0, classOf[LiabilityData]))
    }

  when(mockLiabilityNilReturnValidator.validate(any[LiabilityNilReturn]))
    .thenAnswer { invocation: InvocationOnMock =>
      Validated.Valid(invocation.getArgument(0, classOf[LiabilityNilReturn]))
    }

  when(mockResponseHandler.created())
    .thenReturn(Created(Json.obj("status" -> "Created")))

  when(mockResponseHandler.badRequest(any[String], any[Option[Seq[String]]]))
    .thenAnswer { invocation: InvocationOnMock =>
      val message = invocation.getArgument(0, classOf[String])
      val details = invocation.getArgument(1, classOf[Option[Seq[String]]]).getOrElse(Seq.empty)
      BadRequest(Json.obj("message" -> message, "details" -> details))
    }

  private val controller = new UktrSubmissionController(
    Helpers.stubControllerComponents(),
    mockLiabilityDataValidator,
    mockLiabilityNilReturnValidator,
    mockResponseHandler
  )

  "UktrSubmissionController#submitUktr" should {
    "return Created for a valid UktrSubmissionData submission" in {
      val validData = Json.obj(
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
              "amountOwedUTPR"         -> 100.00
            )
          )
        )
      )

      val request = FakeRequest(POST, "/submitUktr").withJsonBody(validData)
      val result  = controller.submitUktr.apply(request)

      status(result)        shouldBe CREATED: Unit
      contentAsJson(result) shouldBe Json.obj("status" -> "Created")
    }

    "return Created for a valid UktrSubmissionNilReturn submission" in {
      val validNilReturn = Json.obj(
        "accountingPeriodFrom" -> "2023-01-01",
        "accountingPeriodTo"   -> "2023-12-31",
        "obligationMTT"        -> false,
        "electionUKGAAP"       -> true,
        "liabilities"          -> Json.obj("returnType" -> "NIL_RETURN")
      )

      val request = FakeRequest(POST, "/submitUktr")
        .withJsonBody(validNilReturn)

      val result = controller.submitUktr.apply(request)

      status(result)        shouldBe CREATED:                         Unit
      contentAsJson(result) shouldBe Json.obj("status" -> "Created"): Unit
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

      val validationErrors = Seq(
        ValidationError("/liabilities/numberSubGroupDTT", "numberSubGroupDTT must be non-negative"),
        ValidationError("/liabilities/totalLiability", "totalLiability must be a positive number"),
        ValidationError("/liabilities/totalLiabilityIIR", "totalLiabilityIIR must be a positive number"),
        ValidationError("/liabilities/liableEntities", "liableEntities must not be empty")
      )

      // Mock the validator to return invalid result with the specified validation errors
      when(mockLiabilityDataValidator.validate(any[LiabilityData]))
        .thenReturn(Validated.invalid(NonEmptyChain.fromSeq(validationErrors).get))

      val request = FakeRequest(POST, "/submitUktr").withJsonBody(invalidLiabilitiesJson)
      val result  = controller.submitUktr.apply(request)

      // Assertions
      status(result) shouldBe BAD_REQUEST: Unit
      val details = (contentAsJson(result) \ "details").as[Seq[String]]

      details should contain allElementsOf Seq(
        "/liabilities/numberSubGroupDTT: numberSubGroupDTT must be non-negative",
        "/liabilities/totalLiability: totalLiability must be a positive number",
        "/liabilities/totalLiabilityIIR: totalLiabilityIIR must be a positive number",
        "/liabilities/liableEntities: liableEntities must not be empty"
      )
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

      val request = FakeRequest(POST, "/submitUktr").withJsonBody(jsonWithUnknownFields)
      val result  = controller.submitUktr.apply(request)

      status(result) shouldBe BAD_REQUEST: Unit
      val details = (contentAsJson(result) \ "details").as[Seq[String]]

      details should contain allElementsOf Seq(
        "Path: /extraTopLevelField, Errors: unexpected field",
        "Path: /liabilities/unknownLiabilityField, Errors: unexpected field"
      ): Unit
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
          "numberSubGroupDTT"        -> -1,
          "numberSubGroupUTPR"       -> 3,
          "totalLiability"           -> -1000.50,
          "totalLiabilityDTT"        -> 500.25,
          "totalLiabilityIIR"        -> 0,
          "totalLiabilityUTPR"       -> 200.10,
          "liableEntities"           -> Json.arr()
        )
      )

      val request = FakeRequest(POST, "/submitUktr").withJsonBody(invalidSubmissionDataJson)
      val result  = controller.submitUktr.apply(request)

      status(result) shouldBe BAD_REQUEST: Unit
      val details = (contentAsJson(result) \ "details").as[Seq[String]]

      details should contain allElementsOf Seq(
        "/liabilities/numberSubGroupDTT: numberSubGroupDTT must be non-negative",
        "/liabilities/totalLiability: totalLiability must be a positive number",
        "/liabilities/totalLiabilityIIR: totalLiabilityIIR must be a positive number",
        "/liabilities/liableEntities: liableEntities must not be empty"
      ): Unit
    }

    "return BadRequest when UktrSubmissionNilReturn has validation errors" in {
      val invalidNilReturnJson = Json.obj(
        "accountingPeriodFrom" -> "2023-01-01",
        "accountingPeriodTo"   -> "2023-12-31",
        "obligationMTT"        -> false,
        "electionUKGAAP"       -> true,
        "liabilities" -> Json.obj(
          "returnType" -> "INVALID_RETURN_TYPE"
        )
      )

      val request = FakeRequest(POST, "/submitUktr").withJsonBody(invalidNilReturnJson)
      val result  = controller.submitUktr.apply(request)

      status(result)                                 shouldBe BAD_REQUEST:           Unit
      (contentAsJson(result) \ "message").as[String] shouldBe "Invalid JSON format": Unit

      val details = (contentAsJson(result) \ "details").as[Seq[String]]
      details should contain("Path: /liabilities/returnType, Errors: Unknown submission type: INVALID_RETURN_TYPE"): Unit
    }

    "return BadRequest when UktrSubmissionNilReturn has validation errors in liabilities" in {
      val invalidNilReturnJson = Json.obj(
        "accountingPeriodFrom" -> "2023-01-01",
        "accountingPeriodTo"   -> "2023-12-31",
        "obligationMTT"        -> false,
        "electionUKGAAP"       -> true,
        "liabilities" -> Json.obj(
          "returnType" -> "INVALID_RETURN_TYPE"
        )
      )

      val request = FakeRequest(POST, "/submitUktr").withJsonBody(invalidNilReturnJson)
      val result  = controller.submitUktr.apply(request)

      status(result) shouldBe BAD_REQUEST: Unit

      val jsonResponse = contentAsJson(result)
      (jsonResponse \ "message").as[String] shouldBe "Invalid JSON format": Unit

      val details = (jsonResponse \ "details").as[Seq[String]]
      details should contain allElementsOf Seq(
        "Path: /liabilities/returnType, Errors: Unknown submission type: INVALID_RETURN_TYPE"
      ): Unit
    }

    "return BadRequest when UktrSubmissionNilReturn has invalid liabilities" in {
      val invalidNilReturnJson = Json.obj(
        "accountingPeriodFrom" -> "2023-01-01",
        "accountingPeriodTo"   -> "2023-12-31",
        "obligationMTT"        -> false,
        "electionUKGAAP"       -> true,
        "liabilities" -> Json.obj(
          "returnType" -> "INVALID_RETURN_TYPE"
        )
      )

      val request = FakeRequest(POST, "/submitUktr").withJsonBody(invalidNilReturnJson)
      val result  = controller.submitUktr.apply(request)

      status(result) shouldBe BAD_REQUEST: Unit

      val jsonResponse = contentAsJson(result)
      (jsonResponse \ "message").as[String] shouldBe "Invalid JSON format": Unit

      val details = (jsonResponse \ "details").as[Seq[String]]
      details should contain("Path: /liabilities/returnType, Errors: Unknown submission type: INVALID_RETURN_TYPE"): Unit
    }

    "return BadRequest for completely invalid UktrSubmissionNilReturn" in {
      val invalidNilReturnJson = Json.obj(
        "accountingPeriodFrom" -> "INVALID_DATE",
        "liabilities" -> Json.obj(
          "returnType" -> "INVALID_RETURN_TYPE"
        )
      )

      val request = FakeRequest(POST, "/submitUktr").withJsonBody(invalidNilReturnJson)
      val result  = controller.submitUktr.apply(request)

      status(result) shouldBe BAD_REQUEST: Unit

      val details = (contentAsJson(result) \ "details").as[Seq[String]]
      details should contain("Path: /accountingPeriodFrom, Errors: error.expected.date.isoformat"):                  Unit
      details should contain("Path: /liabilities/returnType, Errors: Unknown submission type: INVALID_RETURN_TYPE"): Unit
    }

    "return BadRequest when UktrSubmissionNilReturn has invalid returnType" in {
      val invalidNilReturnJson = Json.obj(
        "accountingPeriodFrom" -> "2023-01-01",
        "accountingPeriodTo"   -> "2023-12-31",
        "obligationMTT"        -> false,
        "electionUKGAAP"       -> true,
        "liabilities" -> Json.obj(
          "returnType" -> "INVALID_RETURN_TYPE"
        )
      )

      val request = FakeRequest(POST, "/submitUktr").withJsonBody(invalidNilReturnJson)
      val result  = controller.submitUktr.apply(request)

      status(result) shouldBe BAD_REQUEST: Unit

      val details = (contentAsJson(result) \ "details").as[Seq[String]]
      details should contain("Path: /liabilities/returnType, Errors: Unknown submission type: INVALID_RETURN_TYPE"): Unit
    }

    "return BadRequest when JSON is missing required fields in liabilities" in {
      val invalidLiabilitiesJson = Json.obj(
        "accountingPeriodFrom" -> "2023-01-01",
        "accountingPeriodTo"   -> "2023-12-31",
        "obligationMTT"        -> true,
        "electionUKGAAP"       -> false,
        "liabilities"          -> Json.obj()
      )

      val request = FakeRequest(POST, "/submitUktr").withJsonBody(invalidLiabilitiesJson)
      val result  = controller.submitUktr.apply(request)

      status(result) shouldBe BAD_REQUEST: Unit
      val details = (contentAsJson(result) \ "details").as[Seq[String]]

      details should contain allElementsOf Seq(
        "Path: /liabilities/totalLiabilityUTPR, Errors: error.path.missing",
        "Path: /liabilities/liableEntities, Errors: error.path.missing",
        "Path: /liabilities/electionUTPRSingleMember, Errors: error.path.missing",
        "Path: /liabilities/totalLiability, Errors: error.path.missing",
        "Path: /liabilities/electionDTTSingleMember, Errors: error.path.missing",
        "Path: /liabilities/numberSubGroupDTT, Errors: error.path.missing",
        "Path: /liabilities/totalLiabilityIIR, Errors: error.path.missing",
        "Path: /liabilities/totalLiabilityDTT, Errors: error.path.missing",
        "Path: /liabilities/numberSubGroupUTPR, Errors: error.path.missing"
      ): Unit
    }

    "return BadRequest when LiabilityNilReturnValidator returns Invalid" in {

      val validNilReturnJson = Json.obj(
        "accountingPeriodFrom" -> "2023-01-01",
        "accountingPeriodTo"   -> "2023-12-31",
        "obligationMTT"        -> false,
        "electionUKGAAP"       -> true,
        "liabilities"          -> Json.obj("returnType" -> "NIL_RETURN")
      )

      val validationError = ValidationError("field", "error message")
      val invalidResult: ValidatedNec[ValidationError, LiabilityNilReturn] =
        Validated.invalidNec(validationError)

      when(mockLiabilityNilReturnValidator.validate(any[LiabilityNilReturn]))
        .thenReturn(invalidResult)

      val request = FakeRequest(POST, "/submitUktr").withJsonBody(validNilReturnJson)
      val result  = controller.submitUktr.apply(request)

      status(result) shouldBe BAD_REQUEST: Unit
      val jsonResponse = contentAsJson(result)
      (jsonResponse \ "message").as[String] shouldBe "Validation failed": Unit

      val details = (jsonResponse \ "details").as[Seq[String]]
      details should contain(s"${validationError.field}: ${validationError.error}")
    }

    "return BadRequest with default error message when no details are provided" in {

      val responseHandler = new ResponseHandler


      val result: Result = responseHandler.badRequest("Invalid JSON format", None) // Returns a Result, not Future[Result]


      status(result) shouldBe BAD_REQUEST
      val jsonResponse = contentAsJson(result)
      (jsonResponse \ "message").as[String] shouldBe "Invalid JSON format"
      (jsonResponse \ "details").as[Seq[String]] should contain("An unexpected error occurred")
    }
  }
}
