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

//package uk.gov.hmrc.pillar2submissionapi.validation
//
//import cats.data.ValidatedNec
//import cats.implicits._
//import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.{LiabilityNilReturn, ReturnType}
//
//object LiabilityNilReturnValidator extends Validator[LiabilityNilReturn] {
//  override def validate(obj: LiabilityNilReturn): ValidatedNec[ValidationError, LiabilityNilReturn] =
//    validateEnum(obj.returnType, ReturnType.values, "returnType").map(_ => obj)
//
//  private def validateEnum[T](value: T, validValues: Seq[T], fieldName: String): ValidatedNec[ValidationError, T] =
//    if (validValues.contains(value)) value.validNec
//    else ValidationError(fieldName, s"$fieldName has an invalid value").invalidNec
//}

package uk.gov.hmrc.pillar2submissionapi.validation

import cats.data.ValidatedNec
import cats.implicits._
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.{LiabilityNilReturn, ReturnType}

object LiabilityNilReturnValidator extends Validator[LiabilityNilReturn] {
  override def validate(obj: LiabilityNilReturn): ValidatedNec[ValidationError, LiabilityNilReturn] =
    validateEnum(obj.returnType.entryName, "returnType").map(_ => obj) // Use entryName here

  // Using enumeratum's withNameOption to safely handle invalid enum values
  private def validateEnum(value: String, fieldName: String): ValidatedNec[ValidationError, String] =
    // Check if the value is a valid enum option
    ReturnType.withNameOption(value) match {
      case Some(_) => value.validNec // Valid value
      case None    => ValidationError(fieldName, s"$fieldName has an invalid value: $value").invalidNec
    }
}
