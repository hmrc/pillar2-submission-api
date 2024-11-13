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
import cats.data.{NonEmptyChain, ValidatedNec}
import cats.implicits._

object ValidationResult {
  type ValidationResult[A] = ValidatedNec[ValidationError, A]

  def valid[A](a: A): ValidationResult[A] = a.validNec

  def invalid[A](error: ValidationError): ValidationResult[A] = error.invalidNec

  def invalidNec[A](errors: NonEmptyChain[ValidationError]): ValidationResult[A] = errors.invalid

  def sequence[A](results: Seq[ValidationResult[A]]): ValidationResult[Seq[A]] =
    results.sequence
}
