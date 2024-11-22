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

import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.LiableEntity
import cats.data.ValidatedNec
import cats.implicits._

object LiableEntityValidator extends Validator[LiableEntity] {
  override def validate(obj: LiableEntity): ValidatedNec[ValidationError, LiableEntity] =
    (
      validateNonEmptyString(obj.ukChargeableEntityName, "ukChargeableEntityName"),
      validateNonEmptyString(obj.idType, "idType"),
      validateNonEmptyString(obj.idValue, "idValue"),
      validatePositiveBigDecimal(obj.amountOwedDTT, "amountOwedDTT"),
      validatePositiveBigDecimal(obj.amountOwedIIR, "amountOwedIIR"),
      validatePositiveBigDecimal(obj.amountOwedUTPR, "amountOwedUTPR")
    ).mapN((_, _, _, _, _, _) => obj)

  private def validateNonEmptyString(value: String, fieldName: String): ValidatedNec[ValidationError, String] =
    if (value == null || value.trim.isEmpty)
      ValidationError(fieldName, s"$fieldName is missing or empty").invalidNec
    else value.validNec

  private def validatePositiveBigDecimal(value: BigDecimal, fieldName: String): ValidatedNec[ValidationError, BigDecimal] =
    if (value == null || value <= 0)
      ValidationError(fieldName, s"$fieldName must be a positive number").invalidNec
    else value.validNec
}
