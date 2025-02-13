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

package uk.gov.hmrc.pillar2submissionapi

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.OptionValues
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.base.IntegrationSpecBase
import uk.gov.hmrc.pillar2submissionapi.controllers.error.AuthenticationError
import uk.gov.hmrc.pillar2submissionapi.models.organisation._
import uk.gov.hmrc.pillar2submissionapi.models.response.Pillar2ErrorResponse
import uk.gov.hmrc.play.bootstrap.http.HttpClientV2Provider

import java.net.URI
import java.time.{Instant, LocalDate}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}
class TestOrganisationISpec extends IntegrationSpecBase with OptionValues {

  lazy val provider: HttpClientV2Provider = app.injector.instanceOf[HttpClientV2Provider]
  lazy val client:   HttpClientV2         = provider.get()
  lazy val baseUrl = s"http://localhost:$port/test-only/organisation"

  private val stubUrl = "/pillar2/test/organisation"

  "TestOrganisationController" when {
    "createTestOrganisation" must {
      "return 201 CREATED for valid request" in {
        stubRequest(
          "POST",
          s"$stubUrl/$plrReference",
          CREATED,
          Json.toJson(validOrganisationWithId)
        )

        val result = Await.result(
          client
            .post(URI.create(baseUrl).toURL)
            .withBody(validRequestJson)
            .execute[HttpResponse],
          5.seconds
        )

        result.status mustEqual CREATED
      }

      "return 400 BAD_REQUEST for invalid request body" in {
        val result = Await.result(
          client
            .post(URI.create(baseUrl).toURL)
            .withBody(invalidRequestJson)
            .execute[HttpResponse],
          5.seconds
        )

        result.status mustEqual BAD_REQUEST
      }

      "return 409 CONFLICT when organisation already exists" in {
        stubRequest(
          "POST",
          s"$stubUrl/$plrReference",
          CONFLICT,
          Json.toJson(Pillar2ErrorResponse("409", s"Organisation with pillar2Id: $plrReference already exists"))
        )

        val result = Await.result(
          client
            .post(URI.create(baseUrl).toURL)
            .withBody(validRequestJson)
            .execute[HttpResponse],
          5.seconds
        )

        result.status mustEqual CONFLICT
      }

      "return 401 UNAUTHORIZED when user cannot be identified" in {
        when(
          mockAuthConnector
            .authorise[RetrievalsType](any[Predicate](), any[Retrieval[RetrievalsType]]())(any[HeaderCarrier](), any[ExecutionContext]())
        ).thenReturn(Future.failed(AuthenticationError))

        val result = Await.result(
          client
            .post(URI.create(baseUrl).toURL)
            .withBody(validRequestJson)
            .execute[HttpResponse],
          5.seconds
        )

        result.status mustEqual UNAUTHORIZED
      }
    }

    "getTestOrganisation" must {

      "return 200 OK with organisation details" in {
        stubRequest(
          "GET",
          s"$stubUrl/$plrReference",
          OK,
          Json.toJson(validOrganisationWithId)
        )

        val result = Await.result(
          client
            .get(URI.create(baseUrl).toURL)
            .execute[HttpResponse],
          5.seconds
        )

        result.status mustEqual OK
      }

      "return 404 NOT_FOUND when organisation doesn't exist" in {
        stubRequest(
          "GET",
          s"$stubUrl/$plrReference",
          NOT_FOUND,
          Json.toJson(Pillar2ErrorResponse("404", s"Organisation not found for pillar2Id: $plrReference"))
        )

        val result = Await.result(
          client
            .get(URI.create(baseUrl).toURL)
            .execute[HttpResponse],
          5.seconds
        )

        result.status mustEqual NOT_FOUND
      }
    }

    "updateTestOrganisation" must {

      "return 200 OK for valid update request" in {
        stubRequest(
          "PUT",
          s"$stubUrl/$plrReference",
          OK,
          Json.toJson(validOrganisationWithId)
        )

        val result = Await.result(
          client
            .put(URI.create(baseUrl).toURL)
            .withBody(validRequestJson)
            .execute[HttpResponse],
          5.seconds
        )

        result.status mustEqual OK
      }

      "return 404 NOT_FOUND when updating non-existent organisation" in {
        stubRequest(
          "PUT",
          s"$stubUrl/$plrReference",
          NOT_FOUND,
          Json.toJson(Pillar2ErrorResponse("404", s"Organisation not found for pillar2Id: $plrReference"))
        )

        val result = Await.result(
          client
            .put(URI.create(baseUrl).toURL)
            .withBody(validRequestJson)
            .execute[HttpResponse],
          5.seconds
        )

        result.status mustEqual NOT_FOUND
      }
    }

    "deleteTestOrganisation" must {

      "return 204 NO_CONTENT for successful deletion" in {
        stubRequest(
          "DELETE",
          s"$stubUrl/$plrReference",
          NO_CONTENT,
          JsObject.empty
        )

        val result = Await.result(
          client
            .delete(URI.create(baseUrl).toURL)
            .execute[HttpResponse],
          5.seconds
        )

        result.status mustEqual NO_CONTENT
      }

      "return 404 NOT_FOUND when deleting non-existent organisation" in {
        stubRequest(
          "DELETE",
          s"$stubUrl/$plrReference",
          NOT_FOUND,
          Json.toJson(Pillar2ErrorResponse("404", s"Organisation not found for pillar2Id: $plrReference"))
        )

        val result = Await.result(
          client
            .delete(URI.create(baseUrl).toURL)
            .execute[HttpResponse],
          5.seconds
        )

        result.status mustEqual NOT_FOUND
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

  val validOrganisation = TestOrganisation(
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

  val validOrganisationWithId = TestOrganisationWithId(
    pillar2Id = plrReference,
    organisation = validOrganisation
  )
}
