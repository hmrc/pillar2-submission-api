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

package uk.gov.hmrc.pillar2submissionapi.models.organisation

import play.api.libs.json._

enum AccountActivityScenario:
  case DTT_CHARGE
  case FULLY_PAID_CHARGE
  case FULLY_PAID_CHARGE_WITH_SPLIT_PAYMENTS
  case REPAYMENT_INTEREST
  case DTT_DETERMINATION
  case DTT_IIR_UTPR
  case ACCRUED_INTEREST
  case DTT_IIR_UTPR_INTEREST
  case DTT_IIR_UTPR_DETERMINATION
  case DTT_IIR_UTPR_DISCOVERY
  case DTT_IIR_UTPR_OVERPAID_CLAIM
  case UKTR_DTT_UKTR_MTT_LATE_FILING_PENALTY
  case ORN_GIR_DTT_UKTR_MTT_LATE_FILING_PENALTY
  case POTENTIAL_LOST_REVENUE_PENALTY
  case SCHEDULE_36_PENALTY
  case RECORD_KEEPING_PENALTY
  case REPAYMENT_CREDIT
  case INTEREST_REPAYMENT_CREDIT

object AccountActivityScenario:
  given format: Format[AccountActivityScenario] = new Format[AccountActivityScenario]:
    def reads(json: JsValue): JsResult[AccountActivityScenario] =
      json.validate[String].flatMap { value =>
        AccountActivityScenario.values.find(_.toString == value) match
          case Some(scenario) => JsSuccess(scenario)
          case None           =>
            JsError(s"Invalid accountActivityScenario: '$value'. Valid values are: ${AccountActivityScenario.values.map(_.toString).mkString(", ")}")
      }

    def writes(scenario: AccountActivityScenario): JsValue =
      JsString(scenario.toString)
