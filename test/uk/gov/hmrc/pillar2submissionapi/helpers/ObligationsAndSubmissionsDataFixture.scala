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

package uk.gov.hmrc.pillar2submissionapi.helpers

import uk.gov.hmrc.pillar2submissionapi.models.obligationsandsubmissions.ObligationStatus.{Fulfilled, Open}
import uk.gov.hmrc.pillar2submissionapi.models.obligationsandsubmissions.ObligationType.{GIR, UKTR}
import uk.gov.hmrc.pillar2submissionapi.models.obligationsandsubmissions.SubmissionType.BTN
import uk.gov.hmrc.pillar2submissionapi.models.obligationsandsubmissions._

import java.time.{LocalDate, ZonedDateTime}

trait ObligationsAndSubmissionsDataFixture {
  val fromDate:      String    = "2024-01-01"
  val localDateFrom: LocalDate = LocalDate.parse(fromDate)
  val toDate:        String    = "2024-12-31"
  val localDateTo:   LocalDate = LocalDate.parse(toDate)

  val obligationsAndSubmissionsSuccessResponse: ObligationsAndSubmissionsSuccessResponse = ObligationsAndSubmissionsSuccessResponse(
    ZonedDateTime.now(),
    Seq(
      AccountingPeriodDetails(
        LocalDate.now(),
        LocalDate.now(),
        LocalDate.now(),
        underEnquiry = false,
        Seq(
          Obligation(
            UKTR,
            Fulfilled,
            canAmend = false,
            Some(Seq(Submission(BTN, ZonedDateTime.now(), None)))
          ),
          Obligation(GIR, Open, canAmend = true, None)
        )
      )
    )
  )
}
