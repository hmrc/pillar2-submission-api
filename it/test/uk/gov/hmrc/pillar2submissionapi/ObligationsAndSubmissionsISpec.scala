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
import org.scalatest.OptionValues
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.pillar2submissionapi.base.IntegrationSpecBase
import uk.gov.hmrc.pillar2submissionapi.controllers.obligationsandsubmissions.routes
import uk.gov.hmrc.pillar2submissionapi.helpers.ObligationsAndSubmissionsDataFixture
import uk.gov.hmrc.pillar2submissionapi.helpers.WireMockServerHandler
import uk.gov.hmrc.pillar2submissionapi.models.obligationsandsubmissions.ObligationsAndSubmissionsSuccessResponse
import uk.gov.hmrc.pillar2submissionapi.models.response.Pillar2ErrorResponse
import uk.gov.hmrc.play.bootstrap.http.HttpClientV2Provider

import java.net.URI
import scala.concurrent.duration.DurationInt
import scala.concurrent.Await

class ObligationsAndSubmissionsISpec
    extends IntegrationSpecBase
    with OptionValues
    with ObligationsAndSubmissionsDataFixture
    with WireMockServerHandler {

  lazy val provider: HttpClientV2Provider = app.injector.instanceOf[HttpClientV2Provider]
  lazy val client:   HttpClientV2         = provider.get()

  private val backendUrl = (from: String, to: String) => s"/report-pillar2-top-up-taxes/obligations-and-submissions/$from/$to"

  private def request(from: String, to: String): RequestBuilder = {
    val url = s"http://localhost:$port${routes.ObligationsAndSubmissionsController.retrieveData(from, to).url}"
    client
      .get(URI.create(url).toURL)
      .setHeader("X-Pillar2-Id" -> plrReference, "Authorization" -> "bearerToken")
  }

  "ObligationsAndSubmissionsController" when {
    "retrieveData as an organisation" must {

      "return 200 OK with data for valid dates and forward X-Pillar2-Id" in {
        val from = fromDate
        val to   = toDate

        stubRequest(
          method = "GET",
          expectedUrl = backendUrl(from, to),
          expectedStatus = OK,
          body = Json.toJson(obligationsAndSubmissionsSuccessResponse),
          headers = Map("X-Pillar2-Id" -> plrReference)
        )

        val result = Await.result(request(from, to).execute[ObligationsAndSubmissionsSuccessResponse], 5.seconds)

        result.accountingPeriodDetails.nonEmpty mustBe true

        server.verify(
          getRequestedFor(urlEqualTo(backendUrl(from, to))).withHeader("X-Pillar2-Id", equalTo(plrReference))
        )
      }

      "return 422 UNPROCESSABLE_ENTITY when backend validates parameters" in {
        val from = fromDate
        val to   = toDate

        stubRequest(
          method = "GET",
          expectedUrl = backendUrl(from, to),
          expectedStatus = UNPROCESSABLE_ENTITY,
          body = Json.obj("code" -> "003", "message" -> "Invalid request")
        )

        val result = Await.result(request(from, to).execute[HttpResponse], 5.seconds)

        result.status mustEqual UNPROCESSABLE_ENTITY
        val error = result.json.as[Pillar2ErrorResponse]
        error.code mustEqual "003"
        error.message mustEqual "Invalid request"
      }

      "return 500 INTERNAL_SERVER_ERROR for backend error" in {
        val from = fromDate
        val to   = toDate

        stubRequest(
          method = "GET",
          expectedUrl = backendUrl(from, to),
          expectedStatus = INTERNAL_SERVER_ERROR,
          body = Json.obj("code" -> "500", "message" -> "Internal Server Error")
        )

        val result = Await.result(request(from, to).execute[HttpResponse], 5.seconds)

        result.status mustEqual INTERNAL_SERVER_ERROR
        val error = result.json.as[Pillar2ErrorResponse]
        error.code mustEqual "500"
        error.message mustEqual "Internal Server Error"
      }

      "return 400 BAD_REQUEST for invalid date range" in {
        val invalidFrom = toDate
        val invalidTo   = fromDate

        val result = Await.result(request(invalidFrom, invalidTo).execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
        val error = result.json.as[Pillar2ErrorResponse]
        error.code mustEqual "INVALID_DATE_RANGE"
        error.message mustEqual "Invalid date range"
      }

      "return 400 BAD_REQUEST for invalid date format" in {
        val invalidFrom = "2024-13-40"
        val invalidTo   = toDate

        val result = Await.result(request(invalidFrom, invalidTo).execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
        val error = result.json.as[Pillar2ErrorResponse]
        error.code mustEqual "INVALID_DATE_FORMAT"
        error.message mustEqual "Invalid date format"
      }

      "return 400 BAD_REQUEST when missing parameters" in {
        val url = s"http://localhost:$port/obligations-and-submissions?fromDate=$fromDate"
        val req = client.get(URI.create(url).toURL).setHeader("X-Pillar2-Id" -> plrReference, "Authorization" -> "bearerToken")

        val result = Await.result(req.execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
        val error = result.json.as[Pillar2ErrorResponse]
        error.code mustEqual "BAD_REQUEST"
        error.message mustEqual "Invalid request"
      }

      "return 500 INTERNAL_SERVER_ERROR when receiving malformed JSON on success" in {
        val from = fromDate
        val to   = toDate

        stubRequest(
          method = "GET",
          expectedUrl = backendUrl(from, to),
          expectedStatus = OK,
          body = Json.obj("unexpected" -> "structure")
        )

        val result = Await.result(request(from, to).execute[HttpResponse], 5.seconds)

        result.status mustEqual INTERNAL_SERVER_ERROR
        val error = result.json.as[Pillar2ErrorResponse]
        error.code mustEqual "500"
        error.message mustEqual "Internal Server Error"
      }
    }
  }
}


