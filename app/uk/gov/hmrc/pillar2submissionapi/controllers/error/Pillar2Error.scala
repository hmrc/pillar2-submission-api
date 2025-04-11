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

package uk.gov.hmrc.pillar2submissionapi.controllers.error

sealed trait Pillar2Error extends Exception {
  val code:    String
  val message: String
}

case object InvalidDateRange extends Pillar2Error {
  val code    = "INVALID_DATE_RANGE"
  val message = "Invalid date range"
}

case object InvalidDateFormat extends Pillar2Error {
  val code    = "INVALID_DATE_FORMAT"
  val message = "Invalid date format"
}

case object InvalidJson extends Pillar2Error {
  val code    = "INVALID_JSON"
  val message = "Invalid JSON payload"
}

case object EmptyRequestBody extends Pillar2Error {
  val code    = "EMPTY_REQUEST_BODY"
  val message = "Empty request body"
}

case object UnexpectedResponse extends Pillar2Error {
  val code:    String = "500"
  val message: String = "Internal Server Error"
}

case object AuthenticationError extends Pillar2Error {
  val code = "003"
  val message: String = "Not authorized"
}

case class NoSubscriptionData(pillar2Id: String) extends Pillar2Error {
  val code = "004"
  val message: String = s"No Pillar2 subscription found for $pillar2Id"
}

case class MissingHeader(message: String) extends Pillar2Error {
  val code = "MISSING_HEADER"
}

object MissingHeader {
  val MissingPillar2Id: MissingHeader = MissingHeader(
    "Please provide the X-Pillar2-Id header"
  )
}

case object ForbiddenError extends Pillar2Error {
  val code    = "006"
  val message = "Forbidden"
}

case class DownstreamValidationError(code: String, message: String) extends Pillar2Error

case class OrganisationAlreadyExists(pillar2Id: String) extends Pillar2Error {
  override val code:    String = "409"
  override val message: String = s"Organisation with pillar2Id: $pillar2Id already exists"
}

case class OrganisationNotFound(pillar2Id: String) extends Pillar2Error {
  override val code:    String = "404"
  override val message: String = s"Organisation not found for pillar2Id: $pillar2Id"
}

case class DatabaseError(operation: String) extends Pillar2Error {
  override val code:    String = "500"
  override val message: String = s"Failed to $operation organisation due to database error"
}

case object TestEndpointDisabled extends Pillar2Error {
  override val code:    String = "403"
  override val message: String = "Test endpoints are not available in this environment"
}
