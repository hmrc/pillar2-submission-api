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

case object InvalidRequest extends Pillar2Error {
  val code    = "000"
  val message = "Invalid Request"
}

case object InvalidJson extends Pillar2Error {
  val code    = "001"
  val message = "Invalid JSON Payload"
}

case object EmptyRequestBody extends Pillar2Error {
  val code    = "002"
  val message = "Empty body in request"
}

case object UnexpectedResponse extends Pillar2Error {
  val code:    String = "500"
  val message: String = "Internal Server Error"
}

case object AuthenticationError extends Pillar2Error {
  val code = "003"
  val message: String = s"Not authorized"
}

case class NoSubscriptionData(pillar2Id: String) extends Pillar2Error {
  val code = "004"
  val message: String = s"No Pillar2 subscription found for $pillar2Id"
}

case class MissingHeader(message: String) extends Pillar2Error {
  val code = "005"
}

case object ForbiddenError extends Pillar2Error {
  val code    = "006"
  val message = "Forbidden"
}

case class UktrValidationError(code: String, message: String) extends Pillar2Error

case class BTNValidationError(code: String, message: String) extends Pillar2Error

case class ObligationsAndSubmissionsValidationError(code: String, message: String) extends Pillar2Error

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

case class TestEndpointDisabled() extends Pillar2Error {
  override val code:    String = "403"
  override val message: String = "Test endpoints are not available in this environment"
}
