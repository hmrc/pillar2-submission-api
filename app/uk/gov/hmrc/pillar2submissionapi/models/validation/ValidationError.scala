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

package uk.gov.hmrc.pillar2submissionapi.models.validation

sealed trait ValidationError {
  def errorMessage: String
}

object ValidationError {
  case class MandatoryFieldMissing(field: String) extends ValidationError {
    override def errorMessage: String = s"Mandatory field missing: $field"
  }
  
  case class InvalidFieldValue(field: String, value: String) extends ValidationError {
    override def errorMessage: String = s"Invalid value '$value' for field: $field"
  }
  
  case class InvalidDateRange(field: String, message: String) extends ValidationError {
    override def errorMessage: String = s"Invalid date range for $field: $message"
  }
  
  case class InvalidAmount(field: String, amount: BigDecimal) extends ValidationError {
    override def errorMessage: String = s"Invalid amount $amount for field: $field"
  }
} 