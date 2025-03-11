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

package uk.gov.hmrc.pillar2submissionapi.connectors

import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import play.api.{Application, Configuration}
import uk.gov.hmrc.pillar2submissionapi.base.UnitTestBaseSpec
import uk.gov.hmrc.pillar2submissionapi.controllers.error._
import uk.gov.hmrc.pillar2submissionapi.models.organisation.{AccountingPeriod, OrgDetails, TestOrganisation}

import java.time.{Instant, LocalDate}

class TestOrganisationConnectorSpec extends UnitTestBaseSpec {

  lazy val connector: TestOrganisationConnector = app.injector.instanceOf[TestOrganisationConnector]

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(Configuration("microservice.services.stub.port" -> server.port()))
    .build()

  private def url(pillar2Id: String) = s"/pillar2/test/organisation/$pillar2Id"

  "TestOrganisationConnector" when {
    "createTestOrganisation" must {
      "return 201 CREATED for valid request" in {
        stubRequest("POST", url(pillar2Id), CREATED, validResponseJson)

        val result = await(connector.createTestOrganisation(pillar2Id, validOrganisationDetails)(hc))

        result.pillar2Id                                shouldBe pillar2Id
        result.organisation.orgDetails.organisationName shouldBe "Test Organisation Ltd"
      }

      "return 409 CONFLICT when organisation already exists" in {
        val errorResponse = Json.obj(
          "code"    -> "409",
          "message" -> s"Organisation with pillar2Id: $pillar2Id already exists"
        )
        stubRequest("POST", url(pillar2Id), CONFLICT, errorResponse)

        intercept[OrganisationAlreadyExists] {
          await(connector.createTestOrganisation(pillar2Id, validOrganisationDetails)(hc))
        }
      }

      "return DatabaseError when receiving 500 INTERNAL_SERVER_ERROR" in {
        val errorResponse = Json.obj(
          "code"    -> "500",
          "message" -> "Database error"
        )
        stubRequest("POST", url(pillar2Id), INTERNAL_SERVER_ERROR, errorResponse)

        intercept[DatabaseError] {
          await(connector.createTestOrganisation(pillar2Id, validOrganisationDetails)(hc))
        }.operation shouldBe "create"
      }

      "return UnexpectedResponse for any other status code" in {
        stubRequest("POST", url(pillar2Id), BAD_REQUEST, Json.obj())

        intercept[UnexpectedResponse.type] {
          await(connector.createTestOrganisation(pillar2Id, validOrganisationDetails)(hc))
        }
      }
    }

    "getTestOrganisation" must {
      "return organisation details for valid request" in {
        stubRequest("GET", url(pillar2Id), OK, validResponseJson)

        val result = await(connector.getTestOrganisation(pillar2Id)(hc))

        result.pillar2Id                                shouldBe pillar2Id
        result.organisation.orgDetails.organisationName shouldBe "Test Organisation Ltd"
      }

      "return 404 NOT_FOUND when organisation doesn't exist" in {
        val errorResponse = Json.obj(
          "code"    -> "404",
          "message" -> s"Organisation not found for pillar2Id: $pillar2Id"
        )
        stubRequest("GET", url(pillar2Id), NOT_FOUND, errorResponse)

        intercept[OrganisationNotFound] {
          await(connector.getTestOrganisation(pillar2Id)(hc))
        }
      }

      "return UnexpectedResponse for any other status code" in {
        stubRequest("GET", url(pillar2Id), BAD_REQUEST, Json.obj())

        intercept[UnexpectedResponse.type] {
          await(connector.getTestOrganisation(pillar2Id)(hc))
        }
      }
    }

    "updateTestOrganisation" must {
      "return updated organisation details for valid request" in {
        stubRequest("PUT", url(pillar2Id), OK, validResponseJson)

        val result = await(connector.updateTestOrganisation(pillar2Id, validOrganisationDetails)(hc))

        result.pillar2Id                                shouldBe pillar2Id
        result.organisation.orgDetails.organisationName shouldBe "Test Organisation Ltd"
      }

      "return 404 NOT_FOUND when organisation doesn't exist" in {
        val errorResponse = Json.obj(
          "code"    -> "404",
          "message" -> s"Organisation not found for pillar2Id: $pillar2Id"
        )
        stubRequest("PUT", url(pillar2Id), NOT_FOUND, errorResponse)

        intercept[OrganisationNotFound] {
          await(connector.updateTestOrganisation(pillar2Id, validOrganisationDetails)(hc))
        }
      }

      "return DatabaseError when receiving 500 INTERNAL_SERVER_ERROR" in {
        val errorResponse = Json.obj(
          "code"    -> "500",
          "message" -> "Database error"
        )
        stubRequest("PUT", url(pillar2Id), INTERNAL_SERVER_ERROR, errorResponse)

        intercept[DatabaseError] {
          await(connector.updateTestOrganisation(pillar2Id, validOrganisationDetails)(hc))
        }.operation shouldBe "update"
      }

      "return UnexpectedResponse for any other status code" in {
        stubRequest("PUT", url(pillar2Id), BAD_REQUEST, Json.obj())

        intercept[UnexpectedResponse.type] {
          await(connector.updateTestOrganisation(pillar2Id, validOrganisationDetails)(hc))
        }
      }
    }

    "deleteTestOrganisation" must {
      "return 204 NO_CONTENT for valid request" in {
        stubRequest("DELETE", url(pillar2Id), NO_CONTENT, JsObject.empty)

        val result = await(connector.deleteTestOrganisation(pillar2Id)(hc))

        result shouldBe (())
      }

      "return 404 NOT_FOUND when organisation doesn't exist" in {
        val errorResponse = Json.obj(
          "code"    -> "404",
          "message" -> s"Organisation not found for pillar2Id: $pillar2Id"
        )
        stubRequest("DELETE", url(pillar2Id), NOT_FOUND, errorResponse)

        intercept[OrganisationNotFound] {
          await(connector.deleteTestOrganisation(pillar2Id)(hc))
        }
      }

      "return DatabaseError when receiving 500 INTERNAL_SERVER_ERROR" in {
        val errorResponse = Json.obj(
          "code"    -> "500",
          "message" -> "Database error"
        )
        stubRequest("DELETE", url(pillar2Id), INTERNAL_SERVER_ERROR, errorResponse)

        intercept[DatabaseError] {
          await(connector.deleteTestOrganisation(pillar2Id)(hc))
        }.operation shouldBe "Failed to delete organisation and submission data"
      }

      "return UnexpectedResponse for any other status code" in {
        stubRequest("DELETE", url(pillar2Id), BAD_REQUEST, Json.obj())

        intercept[UnexpectedResponse.type] {
          await(connector.deleteTestOrganisation(pillar2Id)(hc))
        }
      }
    }
  }

  val validOrganisationDetails: TestOrganisation = TestOrganisation(
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

  val validResponseJson: JsObject = Json.obj(
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
}
