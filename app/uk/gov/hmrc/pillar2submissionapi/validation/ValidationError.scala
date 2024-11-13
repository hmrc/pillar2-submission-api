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

package uk.gov.hmrc.pillar2submissionapi.validation
trait ValidationError {
  def errorCode:    String
  def errorMessage: String
  def field:        String
}

object ValidationError {
  case class MandatoryFieldMissing(field: String) extends ValidationError {
    override def errorCode:    String = "MANDATORY_FIELD_MISSING"
    override def errorMessage: String = s"Mandatory field is missing"
  }

  case class MaxLengthExceeded(field: String, maxLength: Int) extends ValidationError {
    override def errorCode:    String = "MAX_LENGTH_EXCEEDED"
    override def errorMessage: String = s"Field exceeds maximum length of $maxLength"
  }

  case class InvalidFieldValue(field: String, value: String, allowed: Seq[String]) extends ValidationError {
    override def errorCode:    String = "INVALID_FIELD_VALUE"
    override def errorMessage: String = s"Value '$value' is not one of: ${allowed.mkString(", ")}"
  }

  case class InvalidDateRange(field: String, details: String) extends ValidationError {
    override def errorCode:    String = "INVALID_DATE_RANGE"
    override def errorMessage: String = details
  }

  case class InvalidAmount(field: String, amount: BigDecimal) extends ValidationError {
    override def errorCode:    String = "INVALID_AMOUNT"
    override def errorMessage: String = s"Amount must be non-negative"
  }

  case class InvalidFormat(field: String, expectedFormat: String) extends ValidationError {
    override def errorCode:    String = "INVALID_FORMAT"
    override def errorMessage: String = s"Value does not match expected format: $expectedFormat"
  }

  case class BusinessRuleFailure(field: String, rule: String, details: String) extends ValidationError {
    override def errorCode:    String = "BUSINESS_RULE_FAILURE"
    override def errorMessage: String = s"$rule: $details"
  }
}
