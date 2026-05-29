/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.pillar2submissionapi.models.error

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import uk.gov.hmrc.pillar2submissionapi.models.error.Pillar2Error._

class Pillar2ErrorSpec extends AnyFreeSpec with Matchers {

  "Pillar2Error.getMessage must override the Throwable.getMessage and" - {

    "return the custom detail message for InvalidDateRangeError" in {
      val error = InvalidDateRangeError
      error.getMessage mustBe "Code: 'INVALID_DATE_RANGE' Message: 'Invalid date range'"
    }

    "return the custom detail message for InvalidDateFormatError" in {
      val error = InvalidDateFormatError
      error.getMessage mustBe "Code: 'INVALID_DATE_FORMAT' Message: 'Invalid date format'"
    }

    "return the custom detail message for InvalidJsonError" in {
      val error = InvalidJsonError
      error.getMessage mustBe "Code: 'INVALID_JSON' Message: 'Invalid JSON payload'"
    }

    "return the custom detail message for EmptyRequestBodyError" in {
      val error = EmptyRequestBodyError
      error.getMessage mustBe "Code: 'EMPTY_REQUEST_BODY' Message: 'Empty request body'"
    }

    "return the custom detail message for MissingCredentialsError" in {
      val error = MissingCredentialsError
      error.getMessage mustBe "Code: 'MISSING_CREDENTIALS' Message: 'Authentication information is not provided'"
    }

    "return the custom detail message for InvalidCredentialsError" in {
      val error = InvalidCredentialsError
      error.getMessage mustBe "Code: 'INVALID_CREDENTIALS' Message: 'Invalid Authentication information provided'"
    }

    "return the custom detail message for MissingHeaderError" in {
      val error = MissingHeaderError("X-Test-Header")
      error.getMessage mustBe "Code: 'MISSING_HEADER' Message: 'Please provide the X-Test-Header header'"
    }

    "return the custom detail message for ForbiddenError" in {
      val error = ForbiddenError
      error.getMessage mustBe "Code: 'FORBIDDEN' Message: 'Access to the requested resource is forbidden'"
    }

    "return the custom detail message for IncorrectHeaderValueError" in {
      val error = IncorrectHeaderValueError
      error.getMessage mustBe "Code: 'INCORRECT_HEADER_VALUE' Message: 'X-Pillar2-Id Header value does not match the bearer token'"
    }

    "return the custom detail message for InvalidEnrolmentError" in {
      val error = InvalidEnrolmentError
      error.getMessage mustBe "Code: 'INVALID_ENROLMENT' Message: 'Invalid Pillar 2 enrolment'"
    }

    "return the custom detail message for TestEndpointDisabledError" in {
      val error = TestEndpointDisabledError
      error.getMessage mustBe "Code: 'TEST_ENDPOINT_DISABLED' Message: 'Test endpoints are not available in this environment'"
    }

    "return the custom detail message for ORNNotFoundError" in {
      val error = ORNNotFoundError
      error.getMessage mustBe "Code: 'NOT_FOUND' Message: 'The requested ORN could not be found'"
    }

    "return the custom detail message for AccountActivityNotAvailableError" in {
      val error = AccountActivityNotAvailableError
      error.getMessage mustBe "Code: 'NOT_IMPLEMENTED' Message: 'Account activity is not available in this environment'"
    }

    "return the custom detail message for NoSubscriptionDataError" in {
      val error = NoSubscriptionDataError(pillar2Id = "XAPLR1234567890")
      error.getMessage mustBe "Code: '004' Message: 'No Pillar2 subscription found for XAPLR1234567890'"
    }

    "return the custom detail message for OrganisationNotFoundError" in {
      val error = OrganisationNotFoundError(pillar2Id = "XAPLR1234567890")
      error.getMessage mustBe "Code: '404' Message: 'Organisation not found for pillar2Id: XAPLR1234567890'"
    }

    "return the custom detail message for OrganisationAlreadyExistsError" in {
      val error = OrganisationAlreadyExistsError(pillar2Id = "XAPLR1234567890")
      error.getMessage mustBe "Code: '409' Message: 'Organisation with pillar2Id: XAPLR1234567890 already exists'"
    }

    "return the custom detail message for DatabaseError" in {
      val error = DatabaseError(operation = "delete")
      error.getMessage mustBe "Code: '500' Message: 'Failed to delete organisation due to database error'"
    }

    "return the custom detail message for UnexpectedResponseError" in {
      val error = UnexpectedResponseError
      error.getMessage mustBe "Code: '500' Message: 'An unexpected error occurred'"
    }

    "return the custom detail message for DownstreamValidationError" in {
      val error = DownstreamValidationError(code = "422", message = "Test downstream validation error")
      error.getMessage mustBe "Code: '422' Message: 'Test downstream validation error'"
    }

  }
}
