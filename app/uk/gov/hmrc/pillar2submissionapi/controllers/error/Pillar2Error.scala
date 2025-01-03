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

case object InvalidJson extends Pillar2Error {

  val code    = "001"
  val message = "Invalid JSON payload"
}

case object EmptyRequestBody extends Pillar2Error {
  val code    = "002"
  val message = "No body provided in request"
}

case object UnexpectedResponse extends Pillar2Error {
  val code:    String = "500"
  val message: String = "Internal Server Error"
}

case class AuthenticationError(message: String) extends Pillar2Error {
  val code = "003"
}

case class NoSubscriptionData(pillar2Id: String) extends Pillar2Error {
  val code = "004"
  val message: String = s"No Pillar2 subscription found for $pillar2Id"
}

case class UktrValidationError(code: String, message: String) extends Pillar2Error

case class UnparsableResponse(errors: String) extends Pillar2Error {
  val code:    String = "500"
  val message: String = errors
}
