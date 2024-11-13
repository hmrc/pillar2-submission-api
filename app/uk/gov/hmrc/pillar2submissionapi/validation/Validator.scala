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

import cats.data.ValidatedNec
import uk.gov.hmrc.pillar2submissionapi.models.validation.ValidationError

trait Validator[T] {
  def validate(value: T): ValidatedNec[ValidationError, T]
}

object Validator {
  def apply[T](implicit validator: Validator[T]): Validator[T] = validator
  
  implicit class ValidatorOps[T](value: T) {
    def validate(implicit validator: Validator[T]): ValidatedNec[ValidationError, T] = 
      validator.validate(value)
  }
} 