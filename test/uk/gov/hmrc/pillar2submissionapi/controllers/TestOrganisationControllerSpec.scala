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

package uk.gov.hmrc.pillar2submissionapi.controllers

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import play.api.http.Status.*
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.base.ControllerBaseSpec
import uk.gov.hmrc.pillar2submissionapi.config.AppConfig
import uk.gov.hmrc.pillar2submissionapi.controllers.error.*
import uk.gov.hmrc.pillar2submissionapi.controllers.test.TestOrganisationController
import uk.gov.hmrc.pillar2submissionapi.models.organisation.*

import java.time.{Instant, LocalDate}
import scala.concurrent.Future

class TestOrganisationControllerSpec extends ControllerBaseSpec {

  val mockAppConfig: AppConfig = mock[AppConfig]

  def controller(testEndpointsEnabled: Boolean = true): TestOrganisationController = {
    when(mockAppConfig.testOrganisationEnabled).thenReturn(testEndpointsEnabled)
    new TestOrganisationController(cc, identifierAction, pillar2IdAction, mockTestOrganisationService, mockAppConfig)
  }

  "TestOrganisationController" when {
    "test endpoints are enabled" when {
      "createTestOrganisation" must {
        "return 201 CREATED for valid request" in {
          when(mockTestOrganisationService.createTestOrganisation(eqTo(pillar2Id), any[TestOrganisationRequest])(using any[HeaderCarrier]))
            .thenReturn(Future.successful(validOrganisationDetailsWithId))

          val result = controller().createTestOrganisation(FakeRequest().withHeaders("X-Pillar2-Id" -> pillar2Id).withJsonBody(validRequestJson))

          status(result) mustBe CREATED
          contentAsJson(result) mustBe validResponseJson
        }

        "return InvalidJson for invalid request" in {
          val result = controller().createTestOrganisation(FakeRequest().withHeaders("X-Pillar2-Id" -> pillar2Id).withJsonBody(invalidRequestJson))

          result.shouldFailWith(InvalidJson)
        }

        "return EmptyRequestBody for missing body" in {
          val result = controller().createTestOrganisation(FakeRequest().withHeaders("X-Pillar2-Id" -> pillar2Id))

          result.shouldFailWith(EmptyRequestBody)
        }

        "return InvalidDateRange for invalid accounting period" in {
          val result =
            controller().createTestOrganisation(FakeRequest().withHeaders("X-Pillar2-Id" -> pillar2Id).withJsonBody(invalidAccountingPeriodJson))

          result.shouldFailWith(InvalidDateRange)
        }
      }

      "getTestOrganisation" must {
        "return 200 OK with organisation details" in {
          when(mockTestOrganisationService.getTestOrganisation(eqTo(pillar2Id))(using any[HeaderCarrier]))
            .thenReturn(Future.successful(validOrganisationDetailsWithId))

          val result = controller().getTestOrganisation(FakeRequest().withHeaders("X-Pillar2-Id" -> pillar2Id))

          status(result) mustBe OK
          contentAsJson(result) mustBe validResponseJson
        }
      }

      "updateTestOrganisation" must {
        "return 200 OK for valid request" in {
          when(mockTestOrganisationService.updateTestOrganisation(eqTo(pillar2Id), any[TestOrganisationRequest])(using any[HeaderCarrier]))
            .thenReturn(Future.successful(validOrganisationDetailsWithId))

          val result = controller().updateTestOrganisation(FakeRequest().withHeaders("X-Pillar2-Id" -> pillar2Id).withJsonBody(validRequestJson))

          status(result) mustBe OK
          contentAsJson(result) mustBe validResponseJson
        }

        "return InvalidJson for invalid request" in {
          val result = controller().updateTestOrganisation(FakeRequest().withHeaders("X-Pillar2-Id" -> pillar2Id).withJsonBody(invalidRequestJson))

          result.shouldFailWith(InvalidJson)
        }

        "return EmptyRequestBody for missing body" in {
          val result = controller().updateTestOrganisation(FakeRequest().withHeaders("X-Pillar2-Id" -> pillar2Id))

          result.shouldFailWith(EmptyRequestBody)
        }

        "return InvalidDateRange for invalid accounting period" in {
          val result =
            controller().updateTestOrganisation(FakeRequest().withHeaders("X-Pillar2-Id" -> pillar2Id).withJsonBody(invalidAccountingPeriodJson))

          result.shouldFailWith(InvalidDateRange)
        }
      }

      "deleteTestOrganisation" must {
        "return 204 NO_CONTENT for successful deletion" in {
          when(mockTestOrganisationService.deleteTestOrganisation(eqTo(pillar2Id))(using any[HeaderCarrier]))
            .thenReturn(Future.successful(()))

          val result = controller().deleteTestOrganisation(FakeRequest().withHeaders("X-Pillar2-Id" -> pillar2Id))

          status(result) mustBe NO_CONTENT
        }
      }
    }

    "test endpoints are disabled" when {
      "createTestOrganisation" must {
        "return 403 FORBIDDEN" in {
          val result = controller(testEndpointsEnabled = false).createTestOrganisation(
            FakeRequest().withHeaders("X-Pillar2-Id" -> pillar2Id).withJsonBody(validRequestJson)
          )

          result.shouldFailWith(TestEndpointDisabled)
        }
      }

      "getTestOrganisation" must {
        "return 403 FORBIDDEN" in {
          val result = controller(testEndpointsEnabled = false).getTestOrganisation(FakeRequest().withHeaders("X-Pillar2-Id" -> pillar2Id))

          result.shouldFailWith(TestEndpointDisabled)
        }
      }

      "updateTestOrganisation" must {
        "return 403 FORBIDDEN" in {
          val result = controller(testEndpointsEnabled = false).updateTestOrganisation(
            FakeRequest().withHeaders("X-Pillar2-Id" -> pillar2Id).withJsonBody(validRequestJson)
          )

          result.shouldFailWith(TestEndpointDisabled)
        }
      }

      "deleteTestOrganisation" must {
        "return 403 FORBIDDEN" in {
          val result = controller(testEndpointsEnabled = false).deleteTestOrganisation(FakeRequest().withHeaders("X-Pillar2-Id" -> pillar2Id))

          result.shouldFailWith(TestEndpointDisabled)
        }
      }
    }
  }

  val validRequestJson: JsValue = Json.obj(
    "orgDetails" -> Json.obj(
      "domesticOnly"     -> true,
      "organisationName" -> "Test Organisation Ltd",
      "registrationDate" -> "2024-01-01"
    ),
    "accountingPeriod" -> Json.obj(
      "startDate" -> "2024-01-01",
      "endDate"   -> "2024-12-31"
    )
  )

  val invalidAccountingPeriodJson: JsValue = validRequestJson
    .as[JsObject]
    .deepMerge(Json.obj("accountingPeriod" -> Json.obj("endDate" -> "2000-12-31")))

  val invalidRequestJson: JsValue = Json.obj(
    "invalidField" -> "invalidValue"
  )

  val validResponseJson: JsValue = Json.obj(
    "pillar2Id"    -> pillar2Id,
    "organisation" -> Json.obj(
      "orgDetails" -> Json.obj(
        "domesticOnly"     -> true,
        "organisationName" -> "Test Organisation Ltd",
        "registrationDate" -> "2024-01-01"
      ),
      "accountingPeriod" -> Json.obj(
        "startDate" -> "2024-01-01",
        "endDate"   -> "2024-12-31"
      ),
      "lastUpdated" -> "2024-01-01T12:00:00Z"
    )
  )

  val validOrganisationDetailsWithId: TestOrganisationWithId = TestOrganisationWithId(
    pillar2Id = pillar2Id,
    organisation = TestOrganisation(
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
      lastUpdated = Instant.parse("2024-01-01T12:00:00Z")
    )
  )
}
