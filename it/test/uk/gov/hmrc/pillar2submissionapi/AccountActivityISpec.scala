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

package uk.gov.hmrc.pillar2submissionapi

import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, getRequestedFor, urlEqualTo}
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.http.Status.{BAD_GATEWAY, BAD_REQUEST, INTERNAL_SERVER_ERROR, OK, UNAUTHORIZED, UNPROCESSABLE_ENTITY}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.pillar2submissionapi.base.IntegrationSpecBase
import uk.gov.hmrc.pillar2submissionapi.controllers.accountactivity.routes
import uk.gov.hmrc.pillar2submissionapi.controllers.error.{InvalidDateFormat, InvalidDateRange, UnexpectedResponse}
import uk.gov.hmrc.pillar2submissionapi.helpers.{AccountActivityDataFixture, WireMockServerHandler}
import uk.gov.hmrc.pillar2submissionapi.models.accountactivity.AccountActivitySuccessResponse
import uk.gov.hmrc.pillar2submissionapi.models.response.Pillar2ErrorResponse
import uk.gov.hmrc.play.bootstrap.http.HttpClientV2Provider

import java.net.URI

class AccountActivityISpec
    extends IntegrationSpecBase
    with OptionValues
    with AccountActivityDataFixture
    with WireMockServerHandler
    with ScalaCheckDrivenPropertyChecks
    with ScalaFutures
    with IntegrationPatience {

  lazy val provider: HttpClientV2Provider = app.injector.instanceOf[HttpClientV2Provider]
  lazy val client:   HttpClientV2         = provider.get()

  private val backendEndpoint = (from: String, to: String) => s"/report-pillar2-top-up-taxes/account-activity?fromDate=$from&toDate=$to"

  private def request(from: String, to: String): RequestBuilder = {
    val url = s"http://localhost:$port${routes.AccountActivityController.retrieveAccountActivity(from, to).url}"
    client
      .get(URI.create(url).toURL)
      .setHeader("X-Pillar2-Id" -> plrReference, "Authorization" -> "bearer token")
  }

  "Retrieving account activity" must {
    "return OK on the happy path" in {
      stubRequest(
        method = "GET",
        expectedUrl = backendEndpoint(fromDate, toDate),
        expectedStatus = OK,
        body = accountActivityJsonResponse,
        headers = Map("X-Pillar2-Id" -> plrReference)
      )

      val result = request(fromDate, toDate).execute[HttpResponse].futureValue

      result.status mustBe OK
      result.json mustBe Json.toJson(accountActivityJsonParsed)

      server.verify(
        getRequestedFor(urlEqualTo(backendEndpoint(fromDate, toDate))).withHeader("X-Pillar2-Id", equalTo(plrReference))
      )
    }

    "return bad request on a malformatted date" in {
      val result = request("not-a-date", toDate).execute[HttpResponse].futureValue

      result.status mustBe BAD_REQUEST
      result.json mustBe Json.toJson(Pillar2ErrorResponse(InvalidDateFormat.code, InvalidDateFormat.message))
    }

    "return bad request on an invalid date range" in {
      val result = request(localDateFrom.toString, localDateFrom.minusDays(1).toString).execute[HttpResponse].futureValue

      result.status mustBe BAD_REQUEST
      result.json mustBe Json.toJson(Pillar2ErrorResponse(InvalidDateRange.code, InvalidDateRange.message))
    }

    "return unprocessable entity when the backend does" in {
      val unprocessableBody = Json.obj("code" -> "003", "message" -> "Invalid")

      stubRequest(
        method = "GET",
        expectedUrl = backendEndpoint(fromDate, toDate),
        expectedStatus = UNPROCESSABLE_ENTITY,
        body = unprocessableBody
      )

      val result = request(fromDate, toDate).execute[HttpResponse].futureValue

      result.status mustBe UNPROCESSABLE_ENTITY
      result.json mustBe unprocessableBody

      server.verify(
        getRequestedFor(urlEqualTo(backendEndpoint(fromDate, toDate))).withHeader("X-Pillar2-Id", equalTo(plrReference))
      )
    }

    "return unexpected response for statuses from schema that we can't recover from" in forAll(Gen.oneOf(BAD_REQUEST, UNAUTHORIZED, INTERNAL_SERVER_ERROR)) { status =>
      stubRequest(
        method = "GET",
        expectedUrl = backendEndpoint(fromDate, toDate),
        expectedStatus = status,
        body = Json.obj()
      )

      val result = request(fromDate, toDate).execute[HttpResponse].futureValue

      result.status mustBe INTERNAL_SERVER_ERROR
      result.json mustBe Json.toJson(Pillar2ErrorResponse(UnexpectedResponse.code, UnexpectedResponse.message))

      server.verify(
        getRequestedFor(urlEqualTo(backendEndpoint(fromDate, toDate))).withHeader("X-Pillar2-Id", equalTo(plrReference))
      )
    }

    "return unexpected response for unexpected HTTP statuses" in {
      stubRequest(
        method = "GET",
        expectedUrl = backendEndpoint(fromDate, toDate),
        expectedStatus = BAD_GATEWAY,
        body = Json.obj()
      )

      val result = request(fromDate, toDate).execute[HttpResponse].futureValue

      result.status mustBe INTERNAL_SERVER_ERROR
      result.json mustBe Json.toJson(Pillar2ErrorResponse(UnexpectedResponse.code, UnexpectedResponse.message))

      server.verify(
        getRequestedFor(urlEqualTo(backendEndpoint(fromDate, toDate))).withHeader("X-Pillar2-Id", equalTo(plrReference))
      )
    }
  }

}
