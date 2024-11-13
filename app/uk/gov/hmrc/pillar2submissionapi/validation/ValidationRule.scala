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

import cats.implicits._
import uk.gov.hmrc.pillar2submissionapi.validation.ValidationResult._

trait ValidationRule[T] {
  def validate(value: T): ValidationResult[T]
}

object ValidationRule {
  def apply[T](f: T => ValidationResult[T]): ValidationRule[T] =
    (value: T) => f(value)

  def combine[T](rules: ValidationRule[T]*): ValidationRule[T] =
    (value: T) =>
      rules.foldLeft(value.validNec[ValidationError]) { case (acc, rule) =>
        (acc, rule.validate(value)).mapN((_, _) => value)
      }

  implicit class ValidationRuleOps[T](value: T) {
    def validate(implicit validator: ValidationRule[T]): ValidationResult[T] =
      validator.validate(value)
  }
}
