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
import cats.implicits._
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions._

object LiabilityDataValidator extends Validator[LiabilityData] {
  override def validate(obj: LiabilityData): ValidatedNec[ValidationError, LiabilityData] =
    (
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

  private def validateBoolean(value: Boolean, fieldName: String): ValidatedNec[ValidationError, Boolean] =
    if (value || !value) value.validNec
    else ValidationError(fieldName, s"$fieldName must explicitly be true or false").invalidNec

  private def validateNonNegativeInt(value: Int, fieldName: String): ValidatedNec[ValidationError, Int] =
    if (value >= 0) value.validNec
    else ValidationError(fieldName, s"$fieldName must be non-negative").invalidNec

  private def validatePositiveBigDecimal(value: BigDecimal, fieldName: String): ValidatedNec[ValidationError, BigDecimal] =
    if (value > 0) value.validNec
    else ValidationError(fieldName, s"$fieldName must be a positive number").invalidNec

  private def validateLiableEntities(
    entities: Seq[LiableEntity]
  ): ValidatedNec[ValidationError, Seq[LiableEntity]] =
    if (entities.isEmpty)
      ValidationError("liableEntities", "liableEntities must not be empty").invalidNec
    else
      entities.zipWithIndex.map { case (entity, index) =>
        LiableEntityValidator.validate(entity).leftMap(_.map(e => e.copy(field = s"liableEntities[$index].${e.field}")))
      }.sequence
}
