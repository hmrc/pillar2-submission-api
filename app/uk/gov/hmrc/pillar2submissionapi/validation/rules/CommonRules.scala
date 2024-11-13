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

package uk.gov.hmrc.pillar2submissionapi.validation.rules

import uk.gov.hmrc.pillar2submissionapi.validation.ValidationError
import uk.gov.hmrc.pillar2submissionapi.validation.ValidationResult._
import uk.gov.hmrc.pillar2submissionapi.validation.ValidationRule

import java.time.LocalDate

object CommonRules {
  def nonEmptyString(fieldName: String): ValidationRule[String] =
    ValidationRule(str =>
      if (str.trim.isEmpty) invalid(ValidationError.MandatoryFieldMissing(fieldName))
      else valid(str)
    )
    
  def maxLength(fieldName: String, maxLen: Int): ValidationRule[String] =
    ValidationRule(str =>
      if (str.length > maxLen) invalid(ValidationError.MaxLengthExceeded(fieldName, maxLen))
      else valid(str)
    )
    
  def positiveAmount(fieldName: String): ValidationRule[BigDecimal] =
    ValidationRule {amount =>
      if (amount < 0) invalid(ValidationError.InvalidAmount(fieldName, amount))
      else valid(amount)
    }
    
  def dateRange(fromField: String, toField: String): ValidationRule[(LocalDate, LocalDate)] =
    ValidationRule { case (from, to) =>
      if (to.isBefore(from)) 
        invalid(ValidationError.InvalidDateRange(s"$fromField to $toField", "End date must be after start date"))
      else 
        valid((from, to))
    }
} 