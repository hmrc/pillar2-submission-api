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

package uk.gov.hmrc.pillar2submissionapi.fixtures

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.pillar2submissionapi.models.subscription.*

import java.time.LocalDate

trait SubscriptionDataFixtures {

  val readSubscriptionPath = "/report-pillar2-top-up-taxes/subscription/read-subscription"

  private val accountingPeriodStartDate: LocalDate = LocalDate.of(2024, 1, 6)
  private val accountingPeriodEndDate:   LocalDate = accountingPeriodStartDate.plusYears(1)
  private val accountingPeriodDueDate:   LocalDate = accountingPeriodStartDate.plusMonths(18)

  private val upeDetails: UpeDetails =
    UpeDetails(
      safeId = None,
      customerIdentification1 = Some("12345678"),
      customerIdentification2 = Some("87654321"),
      organisationName = "International Organisation Inc.",
      registrationDate = LocalDate.of(2024, 1, 31),
      domesticOnly = false,
      filingMember = false
    )

  private val upeCorrespondenceAddress: UpeCorrespAddressDetails =
    UpeCorrespAddressDetails(
      addressLine1 = "1 High Street",
      addressLine2 = Some("Egham"),
      addressLine3 = Some("Wycombe"),
      addressLine4 = Some("Surrey"),
      postCode = Some("HP13 6TT"),
      countryCode = "GB"
    )

  private val contactDetails: ContactDetailsType =
    ContactDetailsType(
      name = "Primary Contact",
      telephone = Some("0123 4567 890"),
      emailAddress = "primary.contact@example.com"
    )

  private val filingMemberDetails: FilingMemberDetails =
    FilingMemberDetails(
      safeId = "testSafeId",
      customerIdentification1 = Some("FM_CID1"),
      customerIdentification2 = Some("FM_CID2"),
      organisationName = "Filing Member Ltd"
    )

  val subscriptionData: SubscriptionDataDisplay =
    SubscriptionDataDisplay(
      formBundleNumber = "123456789012",
      upeDetails = upeDetails,
      upeCorrespAddressDetails = upeCorrespondenceAddress,
      primaryContactDetails = contactDetails,
      secondaryContactDetails = None,
      filingMemberDetails = Some(filingMemberDetails),
      accountingPeriod = Some(
        Seq(
          AccountingPeriodDisplay(
            startDate = Some(accountingPeriodStartDate),
            endDate = Some(accountingPeriodEndDate),
            dueDate = Some(accountingPeriodDueDate),
            canAmendStartDate = Some(true),
            canAmendEndDate = Some(true)
          )
        )
      ),
      accountStatus = Some(AccountStatus(false))
    )

  val subscriptionDataDisplayJson: JsValue = Json.parse(
    """{
      |  "formBundleNumber": "123456789012",
      |  "upeDetails": {
      |    "customerIdentification1": "12345678",
      |    "customerIdentification2": "87654321",
      |    "organisationName": "International Organisation Inc.",
      |    "registrationDate": "2024-01-31",
      |    "domesticOnly": false,
      |    "filingMember": false
      |  },
      |  "upeCorrespAddressDetails": {
      |    "addressLine1": "1 High Street",
      |    "addressLine2": "Egham",
      |    "addressLine3": "Wycombe",
      |    "addressLine4": "Surrey",
      |    "postCode": "HP13 6TT",
      |    "countryCode": "GB"
      |  },
      |  "primaryContactDetails": {
      |    "name": "Primary Contact",
      |    "telephone": "0123 4567 890",
      |    "emailAddress": "primary.contact@example.com"
      |  },
      |  "filingMemberDetails": {
      |    "safeId": "testSafeId",
      |    "customerIdentification1": "FM_CID1",
      |    "customerIdentification2": "FM_CID2",
      |    "organisationName": "Filing Member Ltd"
      |  },
      |  "accountingPeriod": [
      |    {
      |      "startDate": "2024-01-06",
      |      "endDate": "2025-01-06",
      |      "dueDate": "2025-07-06",
      |      "canAmendStartDate": true,
      |      "canAmendEndDate": true
      |    }
      |  ],
      |  "accountStatus": { "inactive": false }
      |}""".stripMargin
  )

  val subscriptionSuccessJson: JsValue = Json.toJson(SubscriptionDisplayResponse(subscriptionData))

  val invalidSubscriptionJson: JsValue = Json.obj("invalidField" -> "invalidValue")
}
