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

import cats.implicits._
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions._
import uk.gov.hmrc.pillar2submissionapi.validation.ValidationResult._
import uk.gov.hmrc.pillar2submissionapi.validation.ValidationRule
import uk.gov.hmrc.pillar2submissionapi.validation.rules.CommonRules._
object UktrRules {
  val liableEntityRules: ValidationRule[LiableEntity] = ValidationRule.combine(
    ValidationRule(entity => 
      nonEmptyString("ukChargeableEntityName").validate(entity.ukChargeableEntityName)
        .map(_ => entity)
    ),
    ValidationRule(entity =>
      positiveAmount("amountOwedDTT").validate(entity.amountOwedDTT)
        .map(_ => entity)
    ),
  )
  
  val liabilityDataRules: ValidationRule[LiabilityData] = ValidationRule.combine(
    ValidationRule(liability =>
      positiveAmount("totalLiability").validate(liability.totalLiability)
        .map(_ => liability)
    ),
    ValidationRule(liability =>
      sequence(liability.liableEntities.map(liableEntityRules.validate))
        .map(_ => liability)
    )
  )
  
  val submissionRules: ValidationRule[UktrSubmission] = ValidationRule {
    case submission: UktrSubmissionData => validateSubmissionData(submission)
    case submission: UktrSubmissionNilReturn => validateNilReturn(submission)
  }
  
  private def validateSubmissionData(submission: UktrSubmissionData): ValidationResult[UktrSubmission] = {
    (
      dateRange("accountingPeriodFrom", "accountingPeriodTo")
        .validate((submission.accountingPeriodFrom, submission.accountingPeriodTo)),
      liabilityDataRules.validate(submission.liabilities)
    ).mapN((_, _) => submission)
  }
  
  private def validateNilReturn(submission: UktrSubmissionNilReturn): ValidationResult[UktrSubmission] = {
    dateRange("accountingPeriodFrom", "accountingPeriodTo")
      .validate((submission.accountingPeriodFrom, submission.accountingPeriodTo))
      .map(_ => submission)
  }
} 