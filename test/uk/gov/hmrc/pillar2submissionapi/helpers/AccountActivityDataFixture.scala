/*
 * Copyright 2025 HM Revenue & Customs
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

import play.api.libs.json.JsValue
import play.api.libs.json.Json
import uk.gov.hmrc.pillar2submissionapi.models.accountactivity.{AccountActivityClearance, AccountActivitySuccessResponse, AccountActivityTransaction}

import java.time.{LocalDate, ZoneOffset, ZonedDateTime}

trait AccountActivityDataFixture {
  val fromDate:      String    = "2024-01-01"
  val localDateFrom: LocalDate = LocalDate.parse(fromDate)
  val toDate:        String    = "2024-12-31"
  val localDateTo:   LocalDate = LocalDate.parse(toDate)

  val accountActivityJsonResponse: JsValue = Json.parse(getClass.getResourceAsStream("/sample-data/account-activity-response.json"))

  def accountActivityJsonParsed: AccountActivitySuccessResponse = AccountActivitySuccessResponse(
    processingDate = ZonedDateTime.of(2001, 12, 17, 9, 30, 47, 0, ZoneOffset.UTC),
    Seq(
      AccountActivityTransaction(
        transactionType = "Payment",
        transactionDesc = "On Account Pillar 2 (Payment on Account)",
        startDate = None,
        endDate = None,
        accruedInterest = None,
        chargeRefNo = None,
        transactionDate = LocalDate.of(2025, 10, 15),
        dueDate = None,
        originalAmount = BigDecimal("10000"),
        outstandingAmount = Some(BigDecimal("1000")),
        clearedAmount = Some(BigDecimal("9000")),
        standOverAmount = None,
        appealFlag = None,
        clearingDetails = Some(
          Seq(
            AccountActivityClearance(
              transactionDesc = "Pillar 2 UK Tax Return Pillar 2 DTT",
              chargeRefNo = Some("X123456789012"),
              dueDate = Some(LocalDate.of(2025, 12, 31)),
              amount = BigDecimal("2000"),
              clearingDate = LocalDate.of(2025, 10, 15),
              clearingReason = Some("Allocated to Charge")
            ),
            AccountActivityClearance(
              transactionDesc = "Pillar 2 UK Tax Return Pillar 2 MTT IIR",
              chargeRefNo = Some("X123456789012"),
              dueDate = Some(LocalDate.of(2025, 12, 31)),
              amount = BigDecimal("2000"),
              clearingDate = LocalDate.of(2025, 10, 15),
              clearingReason = Some("Allocated to Charge")
            ),
            AccountActivityClearance(
              transactionDesc = "Pillar 2 UK Tax Return Pillar 2 MTT UTPR",
              chargeRefNo = Some("X123456789012"),
              dueDate = Some(LocalDate.of(2025, 12, 31)),
              amount = BigDecimal("2000"),
              clearingDate = LocalDate.of(2025, 10, 15),
              clearingReason = Some("Allocated to Charge")
            ),
            AccountActivityClearance(
              transactionDesc = "Pillar 2 Discovery Assessment Pillar 2 DTT",
              chargeRefNo = Some("X123456789012"),
              dueDate = Some(LocalDate.of(2025, 12, 31)),
              amount = BigDecimal("3000"),
              clearingDate = LocalDate.of(2025, 10, 15),
              clearingReason = Some("Allocated to Charge")
            )
          )
        )
      ),
      AccountActivityTransaction(
        transactionType = "Debit",
        transactionDesc = "Pillar 2 UK Tax Return Pillar 2 DTT",
        startDate = Some(LocalDate.of(2025, 1, 1)),
        endDate = Some(LocalDate.of(2025, 12, 31)),
        accruedInterest = None,
        chargeRefNo = Some("X123456789012"),
        transactionDate = LocalDate.of(2025, 2, 15),
        dueDate = Some(LocalDate.of(2025, 12, 31)),
        originalAmount = BigDecimal("2000"),
        outstandingAmount = None,
        clearedAmount = Some(BigDecimal("2000")),
        standOverAmount = None,
        appealFlag = None,
        clearingDetails = Some(
          Seq(
            AccountActivityClearance(
              transactionDesc = "On Account Pillar 2 (Payment on Account)",
              chargeRefNo = None,
              dueDate = None,
              amount = BigDecimal("2000"),
              clearingDate = LocalDate.of(2025, 10, 15),
              clearingReason = Some("Cleared by Payment")
            )
          )
        )
      ),
      AccountActivityTransaction(
        transactionType = "Debit",
        transactionDesc = "Pillar 2 UK Tax Return Pillar 2 MTT IIR",
        startDate = Some(LocalDate.of(2025, 1, 1)),
        endDate = Some(LocalDate.of(2025, 12, 31)),
        accruedInterest = None,
        chargeRefNo = Some("X123456789012"),
        transactionDate = LocalDate.of(2025, 2, 15),
        dueDate = Some(LocalDate.of(2025, 12, 31)),
        originalAmount = BigDecimal("2000"),
        outstandingAmount = None,
        clearedAmount = Some(BigDecimal("2000")),
        standOverAmount = None,
        appealFlag = None,
        clearingDetails = Some(
          Seq(
            AccountActivityClearance(
              transactionDesc = "On Account Pillar 2 (Payment on Account)",
              chargeRefNo = None,
              dueDate = None,
              amount = BigDecimal("2000"),
              clearingDate = LocalDate.of(2025, 10, 15),
              clearingReason = Some("Cleared by Payment")
            )
          )
        )
      ),
      AccountActivityTransaction(
        transactionType = "Debit",
        transactionDesc = "Pillar 2 UK Tax Return Pillar 2 MTT UTPR",
        startDate = Some(LocalDate.of(2025, 1, 1)),
        endDate = Some(LocalDate.of(2025, 12, 31)),
        accruedInterest = None,
        chargeRefNo = Some("X123456789012"),
        transactionDate = LocalDate.of(2025, 2, 15),
        dueDate = Some(LocalDate.of(2025, 12, 31)),
        originalAmount = BigDecimal("2000"),
        outstandingAmount = None,
        clearedAmount = Some(BigDecimal("2000")),
        standOverAmount = None,
        appealFlag = None,
        clearingDetails = Some(
          Seq(
            AccountActivityClearance(
              transactionDesc = "On Account Pillar 2 (Payment on Account)",
              chargeRefNo = None,
              dueDate = None,
              amount = BigDecimal("2000"),
              clearingDate = LocalDate.of(2025, 10, 15),
              clearingReason = Some("Cleared by Payment")
            )
          )
        )
      ),
      AccountActivityTransaction(
        transactionType = "Debit",
        transactionDesc = "Pillar 2 Discovery Assessment Pillar 2 DTT",
        startDate = Some(LocalDate.of(2025, 1, 1)),
        endDate = Some(LocalDate.of(2025, 12, 31)),
        accruedInterest = None,
        chargeRefNo = Some("XD23456789012"),
        transactionDate = LocalDate.of(2025, 2, 15),
        dueDate = Some(LocalDate.of(2025, 12, 31)),
        originalAmount = BigDecimal("3000"),
        outstandingAmount = None,
        clearedAmount = Some(BigDecimal("3000")),
        standOverAmount = None,
        appealFlag = None,
        clearingDetails = Some(
          Seq(
            AccountActivityClearance(
              transactionDesc = "On Account Pillar 2 (Payment on Account)",
              chargeRefNo = None,
              dueDate = None,
              amount = BigDecimal("3000"),
              clearingDate = LocalDate.of(2025, 10, 15),
              clearingReason = Some("Cleared by Payment")
            )
          )
        )
      ),
      AccountActivityTransaction(
        transactionType = "Debit",
        transactionDesc = "Pillar 2 Determination Pillar 2 MTT IIR",
        startDate = Some(LocalDate.of(2026, 1, 1)),
        endDate = Some(LocalDate.of(2026, 12, 31)),
        accruedInterest = Some(BigDecimal("35")),
        chargeRefNo = Some("XDT3456789012"),
        transactionDate = LocalDate.of(2027, 2, 15),
        dueDate = Some(LocalDate.of(2028, 3, 31)),
        originalAmount = BigDecimal("3100"),
        outstandingAmount = Some(BigDecimal("3100")),
        clearedAmount = None,
        standOverAmount = None,
        appealFlag = None,
        clearingDetails = None
      ),
      AccountActivityTransaction(
        transactionType = "Debit",
        transactionDesc = "Pillar 2 Overpaid Claim Assmt Pillar 2 MTT UTPR",
        startDate = Some(LocalDate.of(2026, 1, 1)),
        endDate = Some(LocalDate.of(2026, 12, 31)),
        accruedInterest = None,
        chargeRefNo = Some("XOC3456789012"),
        transactionDate = LocalDate.of(2027, 2, 15),
        dueDate = Some(LocalDate.of(2028, 3, 31)),
        originalAmount = BigDecimal("4100"),
        outstandingAmount = Some(BigDecimal("4100")),
        clearedAmount = None,
        standOverAmount = Some(BigDecimal("4100")),
        appealFlag = Some(true),
        clearingDetails = None
      ),
      AccountActivityTransaction(
        transactionType = "Credit",
        transactionDesc = "Pillar 2 UKTR RPI Pillar 2 OECD RPI",
        startDate = None,
        endDate = None,
        accruedInterest = None,
        chargeRefNo = Some("XR23456789012"),
        transactionDate = LocalDate.of(2025, 3, 15),
        dueDate = None,
        originalAmount = BigDecimal("-100"),
        outstandingAmount = Some(BigDecimal("-100")),
        clearedAmount = None,
        standOverAmount = None,
        appealFlag = None,
        clearingDetails = None
      ),
      AccountActivityTransaction(
        transactionType = "Debit",
        transactionDesc = "Pillar 2 UKTR DTT LFP AUTO PEN",
        startDate = Some(LocalDate.of(2024, 1, 1)),
        endDate = Some(LocalDate.of(2024, 12, 31)),
        accruedInterest = None,
        chargeRefNo = Some("XPN3456789012"),
        transactionDate = LocalDate.of(2026, 7, 1),
        dueDate = Some(LocalDate.of(2026, 7, 31)),
        originalAmount = BigDecimal("100"),
        outstandingAmount = Some(BigDecimal("100")),
        clearedAmount = None,
        standOverAmount = None,
        appealFlag = None,
        clearingDetails = None
      ),
      AccountActivityTransaction(
        transactionType = "Debit",
        transactionDesc = "Pillar 2 UKTR Interest Pillar 2 DTT Int",
        startDate = Some(LocalDate.of(2024, 1, 1)),
        endDate = Some(LocalDate.of(2024, 12, 31)),
        accruedInterest = None,
        chargeRefNo = Some("XIN3456789012"),
        transactionDate = LocalDate.of(2025, 10, 15),
        dueDate = Some(LocalDate.of(2025, 10, 15)),
        originalAmount = BigDecimal("35"),
        outstandingAmount = Some(BigDecimal("35")),
        clearedAmount = None,
        standOverAmount = None,
        appealFlag = None,
        clearingDetails = None
      ),
      AccountActivityTransaction(
        transactionType = "Debit",
        transactionDesc = "Pillar 2 Record Keeping Pen TG PEN",
        startDate = None,
        endDate = None,
        accruedInterest = None,
        chargeRefNo = Some("XIN3456789012"),
        transactionDate = LocalDate.of(2026, 6, 30),
        dueDate = Some(LocalDate.of(2026, 7, 30)),
        originalAmount = BigDecimal("3500"),
        outstandingAmount = Some(BigDecimal("3500")),
        clearedAmount = None,
        standOverAmount = None,
        appealFlag = None,
        clearingDetails = None
      )
    )
  )
}
