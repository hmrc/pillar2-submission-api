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

import org.scalatest.OptionValues
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.pillar2submissionapi.base.IntegrationSpecBase
import uk.gov.hmrc.play.bootstrap.http.HttpClientV2Provider

import java.net.URI
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class GlobeInformationReturnISpec extends IntegrationSpecBase with OptionValues {

  lazy val provider: HttpClientV2Provider = app.injector.instanceOf[HttpClientV2Provider]
  lazy val client:   HttpClientV2         = provider.get()
  lazy val baseUrl = s"http://localhost:$port/setup/globe-information-return"

  private val stubUrl = "/pillar2/test/globe-information-return"

  "GIRController" when {
    "createGIR" must {
      "return 201 CREATED for valid request" in {
        stubRequest(
          "POST",
          stubUrl,
          CREATED,
          Json.obj("success" -> Json.obj("processingDate" -> "2024-01-01T12:00:00Z"))
        )

        val result = Await.result(
          client
            .post(URI.create(baseUrl).toURL)
            .withBody(validRequestJson)
            .setHeader("X-Pillar2-Id" -> plrReference)
            .setHeader("Authorization" -> "bearerToken")
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
            .setHeader("X-Pillar2-Id" -> plrReference)
            .setHeader("Authorization" -> "bearerToken")
            .execute[HttpResponse],
          5.seconds
        )

        result.status mustEqual BAD_REQUEST
      }

      "return 422 UNPROCESSABLE_ENTITY for unprocessable entity" in {
        stubRequest(
          "POST",
          stubUrl,
          UNPROCESSABLE_ENTITY,
          Json.obj("errors" -> Json.obj("code" -> "093", "text" -> "Invalid Return"))
        )

        val result = Await.result(
          client
            .post(URI.create(baseUrl).toURL)
            .withBody(validRequestJson)
            .setHeader("X-Pillar2-Id" -> plrReference)
            .setHeader("Authorization" -> "bearerToken")
            .execute[HttpResponse],
          5.seconds
        )

        result.status mustEqual UNPROCESSABLE_ENTITY
      }

      "return 500 INTERNAL_SERVER_ERROR for backend error" in {
        stubRequest(
          "POST",
          stubUrl,
          INTERNAL_SERVER_ERROR,
          Json.obj("code" -> "500", "message" -> "Database error")
        )

        val result = Await.result(
          client
            .post(URI.create(baseUrl).toURL)
            .withBody(validRequestJson)
            .setHeader("X-Pillar2-Id" -> plrReference)
            .setHeader("Authorization" -> "bearerToken")
            .execute[HttpResponse],
          5.seconds
        )

        result.status mustEqual INTERNAL_SERVER_ERROR
      }
    }
  }

  val validRequestJson: JsValue = Json.obj(
    "accountingPeriodFrom" -> "2024-01-01",
    "accountingPeriodTo"   -> "2024-12-31"
  )

  val invalidRequestJson: JsValue = Json.obj(
    "badField" -> "badValue"
  )
}
