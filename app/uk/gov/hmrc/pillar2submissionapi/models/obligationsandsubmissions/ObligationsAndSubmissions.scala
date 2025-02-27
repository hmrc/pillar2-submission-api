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

package uk.gov.hmrc.pillar2submissionapi.models.obligationsandsubmissions

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import play.api.libs.json.{Json, OFormat}

import java.time.{LocalDate, ZonedDateTime}

case class ObligationsAndSubmissions(fromDate: LocalDate, toDate: LocalDate) {
  def validDateRange: Boolean = fromDate.isBefore(toDate)
}

object ObligationsAndSubmissions {
  implicit val format: OFormat[ObligationsAndSubmissions] = Json.format[ObligationsAndSubmissions]
}

case class AccountingPeriodDetails(
  startDate:    LocalDate,
  endDate:      LocalDate,
  dueDate:      LocalDate,
  underEnquiry: Boolean,
  obligations:  Seq[Obligation]
)

object AccountingPeriodDetails {
  implicit val format: OFormat[AccountingPeriodDetails] = Json.format[AccountingPeriodDetails]
}

case class Obligation(obligationType: ObligationType, status: ObligationStatus, canAmend: Boolean, submissions: Seq[Submission])

object Obligation {
  implicit val format: OFormat[Obligation] = Json.format[Obligation]
}

sealed trait ObligationStatus extends EnumEntry
object ObligationStatus extends Enum[ObligationStatus] with PlayJsonEnum[ObligationStatus] {
  val values: IndexedSeq[ObligationStatus] = findValues

  case object Open extends ObligationStatus
  case object Fulfilled extends ObligationStatus
}

sealed trait ObligationType extends EnumEntry
object ObligationType extends Enum[ObligationType] with PlayJsonEnum[ObligationType] {
  val values: IndexedSeq[ObligationType] = findValues

  case object Pillar2TaxReturn extends ObligationType
  case object GlobeInformationReturn extends ObligationType
}

case class Submission(submissionType: SubmissionType, receivedDate: ZonedDateTime, country: Option[String])

object Submission {
  implicit val format: OFormat[Submission] = Json.format[Submission]
}

sealed trait SubmissionType extends EnumEntry
object SubmissionType extends Enum[SubmissionType] with PlayJsonEnum[SubmissionType] {
  val values: IndexedSeq[SubmissionType] = findValues

  case object UKTR extends SubmissionType
  case object ORN extends SubmissionType
  case object BTN extends SubmissionType
  case object GIR extends SubmissionType
}
