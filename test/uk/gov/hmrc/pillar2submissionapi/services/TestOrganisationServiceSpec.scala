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

package uk.gov.hmrc.pillar2submissionapi.services

import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.base.UnitTestBaseSpec
import uk.gov.hmrc.pillar2submissionapi.models.organisation._

import java.time.{Instant, LocalDate}
import scala.concurrent.Future

class TestOrganisationServiceSpec extends UnitTestBaseSpec {

  val service = new TestOrganisationService(mockTestOrganisationConnector)

  "TestOrganisationService" when {
    "createTestOrganisation" must {
      "return organisation details for valid request" in {
        when(mockTestOrganisationConnector.createTestOrganisation(eqTo(pillar2Id), any[TestOrganisation])(using any[HeaderCarrier]))
          .thenReturn(Future.successful(validOrganisationDetailsWithId))

        val result = await(service.createTestOrganisation(pillar2Id, validOrganisationDetailsRequest))

        result mustBe validOrganisationDetailsWithId
      }
    }

    "getTestOrganisation" must {
      "return organisation details" in {
        when(mockTestOrganisationConnector.getTestOrganisation(eqTo(pillar2Id))(using any[HeaderCarrier]))
          .thenReturn(Future.successful(validOrganisationDetailsWithId))

        val result = await(service.getTestOrganisation(pillar2Id))

        result mustBe validOrganisationDetailsWithId
      }
    }

    "updateTestOrganisation" must {
      "return updated organisation details" in {
        when(mockTestOrganisationConnector.updateTestOrganisation(eqTo(pillar2Id), any[TestOrganisation])(using any[HeaderCarrier]))
          .thenReturn(Future.successful(validOrganisationDetailsWithId))

        val result = await(service.updateTestOrganisation(pillar2Id, validOrganisationDetailsRequest))

        result mustBe validOrganisationDetailsWithId
      }
    }

    "deleteTestOrganisation" must {
      "return unit when successful" in {
        when(mockTestOrganisationConnector.deleteTestOrganisation(eqTo(pillar2Id))(using any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        val result = await(service.deleteTestOrganisation(pillar2Id))

        result mustBe (())
      }
    }
  }

  val validOrganisationDetailsRequest: TestOrganisationRequest = TestOrganisationRequest(
    orgDetails = OrgDetails(
      domesticOnly = true,
      organisationName = "Test Organisation Ltd",
      registrationDate = LocalDate.of(2024, 1, 1)
    ),
    accountingPeriod = AccountingPeriod(
      startDate = LocalDate.of(2024, 1, 1),
      endDate = LocalDate.of(2024, 12, 31),
      None
    )
  )

  val validOrganisationDetailsRequestWithTestData: TestOrganisationRequest = TestOrganisationRequest(
    orgDetails = OrgDetails(
      domesticOnly = true,
      organisationName = "Test Organisation Ltd",
      registrationDate = LocalDate.of(2024, 1, 1)
    ),
    accountingPeriod = AccountingPeriod(
      startDate = LocalDate.of(2024, 1, 1),
      endDate = LocalDate.of(2024, 12, 31),
      None
    ),
    testData = Some(TestData(AccountActivityScenario.DTT_CHARGE))
  )

  val validOrganisationDetails: TestOrganisation = TestOrganisation(
    orgDetails = validOrganisationDetailsRequest.orgDetails,
    accountingPeriod = validOrganisationDetailsRequest.accountingPeriod,
    lastUpdated = Instant.parse("2024-01-01T12:00:00Z")
  )

  val validOrganisationDetailsWithId: TestOrganisationWithId = TestOrganisationWithId(
    pillar2Id = pillar2Id,
    organisation = validOrganisationDetails
  )
}
