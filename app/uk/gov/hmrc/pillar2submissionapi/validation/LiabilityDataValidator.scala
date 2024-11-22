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

import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions._
import cats.data.ValidatedNec
import cats.implicits._

case class ValidationError(field: String, error: String)

trait Validator[A] {
  def validate(obj: A): ValidatedNec[ValidationError, A]
}

object LiabilityDataValidator extends Validator[LiabilityData] {
  override def validate(obj: LiabilityData): ValidatedNec[ValidationError, LiabilityData] = {
    val result = (
      validateBoolean(obj.electionDTTSingleMember, "electionDTTSingleMember"),
      validateBoolean(obj.electionUTPRSingleMember, "electionUTPRSingleMember"),
      validateNonNegativeInt(obj.numberSubGroupDTT, "numberSubGroupDTT"),
      validateNonNegativeInt(obj.numberSubGroupUTPR, "numberSubGroupUTPR"),
      validatePositiveBigDecimal(obj.totalLiability, "totalLiability"),
      validatePositiveBigDecimal(obj.totalLiabilityDTT, "totalLiabilityDTT"),
      validatePositiveBigDecimal(obj.totalLiabilityIIR, "totalLiabilityIIR"),
      validatePositiveBigDecimal(obj.totalLiabilityUTPR, "totalLiabilityUTPR"),
      validateLiableEntities(obj.liableEntities)
    ).mapN((_, _, _, _, _, _, _, _, _) => obj)
    result
  }

  private def validateBoolean(value: Boolean, fieldName: String): ValidatedNec[ValidationError, Boolean] =
    if (Option(value).isDefined) value.validNec
    else ValidationError(fieldName, s"$fieldName is missing").invalidNec

  private def validateNonNegativeInt(value: Int, fieldName: String): ValidatedNec[ValidationError, Int] =
    if (value < 0) ValidationError(fieldName, s"$fieldName must be non-negative").invalidNec
    else value.validNec

  private def validatePositiveBigDecimal(value: BigDecimal, fieldName: String): ValidatedNec[ValidationError, BigDecimal] =
    if (value == null || value <= 0)
      ValidationError(fieldName, s"$fieldName must be a positive number").invalidNec
    else value.validNec

  private def validateLiableEntities(
    entities: Seq[LiableEntity]
  ): ValidatedNec[ValidationError, Seq[LiableEntity]] =
    if (entities == null || entities.isEmpty)
      ValidationError("liableEntities", "liableEntities must not be empty").invalidNec
    else
      entities
        .map(LiableEntityValidator.validate) // Validate each entity
        .sequence // Combine all validations
}
