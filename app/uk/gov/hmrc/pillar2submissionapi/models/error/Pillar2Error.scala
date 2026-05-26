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

package uk.gov.hmrc.pillar2submissionapi.models.error

sealed trait Pillar2Error extends Exception {
  def code:    String
  def message: String

  override def getMessage: String = s"Code: '$code' Message: '$message'"
}

object Pillar2Error {

  case object InvalidDateRangeError extends Pillar2Error {
    val code:    String = "INVALID_DATE_RANGE"
    val message: String = "Invalid date range"
  }

  case object InvalidDateFormatError extends Pillar2Error {
    val code:    String = "INVALID_DATE_FORMAT"
    val message: String = "Invalid date format"
  }

  case object InvalidJsonError extends Pillar2Error {
    val code:    String = "INVALID_JSON"
    val message: String = "Invalid JSON payload"
  }

  case object EmptyRequestBodyError extends Pillar2Error {
    val code:    String = "EMPTY_REQUEST_BODY"
    val message: String = "Empty request body"
  }

  case object MissingCredentialsError extends Pillar2Error {
    val code:    String = "MISSING_CREDENTIALS"
    val message: String = "Authentication information is not provided"
  }

  case object InvalidCredentialsError extends Pillar2Error {
    val code:    String = "INVALID_CREDENTIALS"
    val message: String = "Invalid Authentication information provided"
  }

  final case class MissingHeaderError(headerName: String) extends Pillar2Error {
    val code:    String = "MISSING_HEADER"
    val message: String = s"Please provide the $headerName header"
  }

  case object ForbiddenError extends Pillar2Error {
    val code:    String = "FORBIDDEN"
    val message: String = "Access to the requested resource is forbidden"
  }

  case object IncorrectHeaderValueError extends Pillar2Error {
    val code:    String = "INCORRECT_HEADER_VALUE"
    val message: String = "X-Pillar2-Id Header value does not match the bearer token"
  }

  case object InvalidEnrolmentError extends Pillar2Error {
    val code:    String = "INVALID_ENROLMENT"
    val message: String = "Invalid Pillar 2 enrolment"
  }

  case object TestEndpointDisabledError extends Pillar2Error {
    val code:    String = "TEST_ENDPOINT_DISABLED"
    val message: String = "Test endpoints are not available in this environment"
  }

  case object ORNNotFoundError extends Pillar2Error {
    val code:    String = "NOT_FOUND"
    val message: String = "The requested ORN could not be found"
  }

  case object AccountActivityNotAvailableError extends Pillar2Error {
    val code:    String = "NOT_IMPLEMENTED"
    val message: String = "Account activity is not available in this environment"
  }

  final case class NoSubscriptionDataError(pillar2Id: String) extends Pillar2Error {
    val code:    String = "004"
    val message: String = s"No Pillar2 subscription found for $pillar2Id"
  }

  final case class OrganisationNotFoundError(pillar2Id: String) extends Pillar2Error {
    val code:    String = "404"
    val message: String = s"Organisation not found for pillar2Id: $pillar2Id"
  }

  final case class OrganisationAlreadyExistsError(pillar2Id: String) extends Pillar2Error {
    val code:    String = "409"
    val message: String = s"Organisation with pillar2Id: $pillar2Id already exists"
  }

  final case class DatabaseError(operation: String) extends Pillar2Error {
    val code:    String = "500"
    val message: String = s"Failed to $operation organisation due to database error"
  }

  case object UnexpectedResponseError extends Pillar2Error {
    val code:    String = "500"
    val message: String = "Internal Server Error"
  }

  final case class DownstreamValidationError(code: String, message: String) extends Pillar2Error

}
