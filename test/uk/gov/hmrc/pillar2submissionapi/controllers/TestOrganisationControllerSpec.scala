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

import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{BodyParsers, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.base.ControllerBaseSpec
import uk.gov.hmrc.pillar2submissionapi.config.AppConfig
import uk.gov.hmrc.pillar2submissionapi.controllers.actions.AuthenticatedIdentifierAction
import uk.gov.hmrc.pillar2submissionapi.controllers.error.{EmptyRequestBody, InvalidJson, TestEndpointDisabled}
import uk.gov.hmrc.pillar2submissionapi.controllers.test.TestOrganisationController
import uk.gov.hmrc.pillar2submissionapi.models.organisation._
import uk.gov.hmrc.pillar2submissionapi.models.requests.IdentifierRequest

import java.time.{Instant, LocalDate}
import scala.concurrent.Future

class TestOrganisationControllerSpec extends ControllerBaseSpec {

  val mockAppConfig: AppConfig = mock[AppConfig]

  override val identifierAction: AuthenticatedIdentifierAction = new AuthenticatedIdentifierAction(
    mockAuthConnector,
    new BodyParsers.Default,
    mockAppConfig
  ) {
    override def transform[A](request: Request[A]): Future[IdentifierRequest[A]] =
      Future.successful(IdentifierRequest(request, "internalId", Some("groupID"), userIdForEnrolment = "userId", clientPillar2Id = pillar2Id))
  }

  def controller(testEndpointsEnabled: Boolean = true): TestOrganisationController = {
    when(mockAppConfig.testOrganisationEnabled).thenReturn(testEndpointsEnabled)
    new TestOrganisationController(cc, identifierAction, mockTestOrganisationService, mockAppConfig)
  }

  "TestOrganisationController" when {
    "test endpoints are enabled" when {
      "createTestOrganisation" must {
        "return 201 CREATED for valid request" in {
          when(mockTestOrganisationService.createTestOrganisation(eqTo(pillar2Id), any[TestOrganisationRequest])(any[HeaderCarrier]))
            .thenReturn(Future.successful(validOrganisationDetailsWithId))

          val result = controller().createTestOrganisation(FakeRequest().withJsonBody(validRequestJson))

          status(result) mustBe CREATED
          contentAsJson(result) mustBe validResponseJson
        }

        "return InvalidJson for invalid request" in {
          val result = controller().createTestOrganisation(FakeRequest().withJsonBody(invalidRequestJson))

          result shouldFailWith InvalidJson
        }

        "return EmptyRequestBody for missing body" in {
          val result = controller().createTestOrganisation(FakeRequest())

          result shouldFailWith EmptyRequestBody
        }
      }

      "getTestOrganisation" must {
        "return 200 OK with organisation details" in {
          when(mockTestOrganisationService.getTestOrganisation(eqTo(pillar2Id))(any[HeaderCarrier]))
            .thenReturn(Future.successful(validOrganisationDetailsWithId))

          val result = controller().getTestOrganisation(FakeRequest())

          status(result) mustBe OK
          contentAsJson(result) mustBe validResponseJson
        }
      }

      "updateTestOrganisation" must {
        "return 200 OK for valid request" in {
          when(mockTestOrganisationService.updateTestOrganisation(eqTo(pillar2Id), any[TestOrganisationRequest])(any[HeaderCarrier]))
            .thenReturn(Future.successful(validOrganisationDetailsWithId))

          val result = controller().updateTestOrganisation(FakeRequest().withJsonBody(validRequestJson))

          status(result) mustBe OK
          contentAsJson(result) mustBe validResponseJson
        }

        "return InvalidJson for invalid request" in {
          val result = controller().updateTestOrganisation(FakeRequest().withJsonBody(invalidRequestJson))

          result shouldFailWith InvalidJson
        }

        "return EmptyRequestBody for missing body" in {
          val result = controller().updateTestOrganisation(FakeRequest())

          result shouldFailWith EmptyRequestBody
        }
      }

      "deleteTestOrganisation" must {
        "return 204 NO_CONTENT for successful deletion" in {
          when(mockTestOrganisationService.deleteTestOrganisation(eqTo(pillar2Id))(any[HeaderCarrier]))
            .thenReturn(Future.successful(()))

          val result = controller().deleteTestOrganisation(FakeRequest())

          status(result) mustBe NO_CONTENT
        }
      }
    }

    "test endpoints are disabled" when {
      "createTestOrganisation" must {
        "return 403 FORBIDDEN" in {
          val result = controller(testEndpointsEnabled = false).createTestOrganisation(FakeRequest().withJsonBody(validRequestJson))

          result shouldFailWith TestEndpointDisabled()
        }
      }

      "getTestOrganisation" must {
        "return 403 FORBIDDEN" in {
          val result = controller(testEndpointsEnabled = false).getTestOrganisation(FakeRequest())

          result shouldFailWith TestEndpointDisabled()
        }
      }

      "updateTestOrganisation" must {
        "return 403 FORBIDDEN" in {
          val result = controller(testEndpointsEnabled = false).updateTestOrganisation(FakeRequest().withJsonBody(validRequestJson))

          result shouldFailWith TestEndpointDisabled()
        }
      }

      "deleteTestOrganisation" must {
        "return 403 FORBIDDEN" in {
          val result = controller(testEndpointsEnabled = false).deleteTestOrganisation(FakeRequest())

          result shouldFailWith TestEndpointDisabled()
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

  val invalidRequestJson: JsValue = Json.obj(
    "invalidField" -> "invalidValue"
  )

  val validResponseJson: JsValue = Json.obj(
    "pillar2Id" -> pillar2Id,
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
        endDate = LocalDate.of(2024, 12, 31)
      ),
      lastUpdated = Instant.parse("2024-01-01T12:00:00Z")
    )
  )
}
