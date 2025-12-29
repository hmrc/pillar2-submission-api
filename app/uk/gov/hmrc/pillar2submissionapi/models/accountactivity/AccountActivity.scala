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

package uk.gov.hmrc.pillar2submissionapi.models.accountactivity

import cats.data.NonEmptyList

import java.time.{LocalDate, LocalDateTime}

case class AccountActivity(processedAt: LocalDateTime, transactions: Seq[AccountActivityTransaction])

sealed trait AccountActivityTransaction

case class AccountActivityDebit(
  description:       TransactionDescription,
  startDate:         LocalDate,
  endDate:           LocalDate,
  chargeRef:         ChargeRef,
  transactionDate:   LocalDate,
  dueDate:           LocalDate,
  originalAmount:    BigDecimal,
  accruedInterest:   Option[BigDecimal],
  allocationDetails: Option[AccountActivityAllocationDetails[AccountActivityAllocation]],
  outstandingAmount: Option[BigDecimal],
  appealFlag:        Option[true],
  standOverAmount:   Option[BigDecimal]
) extends AccountActivityTransaction

case class AccountActivityCredit(
  description:       TransactionDescription,
  chargeRef:         ChargeRef,
  transactionDate:   LocalDate,
  originalAmount:    BigDecimal,
  outstandingAmount: BigDecimal
) extends AccountActivityTransaction

case class AccountActivityPayment(
  description:       TransactionDescription,
  transactionDate:   LocalDate,
  originalAmount:    BigDecimal,
  outstandingAmount: BigDecimal,
  allocationDetails: Option[AccountActivityAllocationDetails[AccountActivityAllocation]]
) extends AccountActivityTransaction

case class AccountActivityAllocationDetails[A](
  clearedAmount: BigDecimal,
  allocations:   NonEmptyList[A]
)

case class AccountActivityAllocation(
  fromTransaction: TransactionDescription,
  chargeRef:       ChargeRef,
  dueDate:         LocalDate,
  amount:          BigDecimal,
  clearingDate:    LocalDate,
  clearingReason:  ClearingReason
)

case class AccountActivityFulfilment(
  fromTransaction: TransactionDescription,
  amount:          BigDecimal,
  clearingDate:    LocalDate,
  clearingReason:  ClearingReason
)
