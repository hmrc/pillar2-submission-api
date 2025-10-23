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

import play.api.libs.json.{Format, Json}

import java.time.{Instant, LocalDate}

case class OrgDetails(
  domesticOnly:     Boolean,
  organisationName: String,
  registrationDate: LocalDate
)

object OrgDetails {
  implicit val format: Format[OrgDetails] = Json.format[OrgDetails]
}

case class AccountingPeriod(
  startDate:    LocalDate,
  endDate:      LocalDate,
  underEnquiry: Option[Boolean]
)

object AccountingPeriod {
  implicit val format: Format[AccountingPeriod] = Json.format[AccountingPeriod]
}

case class TestOrganisationRequest(
  orgDetails:       OrgDetails,
  accountingPeriod: AccountingPeriod
)

object TestOrganisationRequest {
  implicit val format: Format[TestOrganisationRequest] = Json.format[TestOrganisationRequest]
}

case class TestOrganisation(
  orgDetails:       OrgDetails,
  accountingPeriod: AccountingPeriod,
  lastUpdated:      Instant = Instant.now()
)

object TestOrganisation {
  implicit val format: Format[TestOrganisation] = Json.format[TestOrganisation]
}

case class TestOrganisationWithId(
  pillar2Id:    String,
  organisation: TestOrganisation
)

object TestOrganisationWithId {
  implicit val format: Format[TestOrganisationWithId] = Json.format[TestOrganisationWithId]
}
