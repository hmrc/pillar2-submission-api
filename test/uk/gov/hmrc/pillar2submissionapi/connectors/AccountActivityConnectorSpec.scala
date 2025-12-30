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

package uk.gov.hmrc.pillar2submissionapi.connectors

import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, getRequestedFor, urlEqualTo}
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.{Application, Configuration}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.base.UnitTestBaseSpec
import uk.gov.hmrc.pillar2submissionapi.helpers.AccountActivityDataFixture

class AccountActivityConnectorSpec extends UnitTestBaseSpec with AccountActivityDataFixture with ScalaFutures {
  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(Configuration("microservice.services.pillar2.port" -> server.port()))
    .build()
  lazy val accountActivityConnector: AccountActivityConnector = app.injector.instanceOf[AccountActivityConnector]

  private val getUrl = s"/report-pillar2-top-up-taxes/account-activity?dateFrom=$fromDate&dateTo=$toDate"

  "AccountActivityConnector" when {
    "retrieving account activity" must {
      "forward the response in the happy path" in {
        given hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("X-Pillar2-Id" -> pillar2Id)

        stubRequestWithPillar2Id("GET", getUrl, OK, accountActivityJsonResponse)

        val result = accountActivityConnector.getAccountActivity(localDateFrom, localDateTo).futureValue

        result.status mustBe OK
        result.body mustBe Json.stringify(accountActivityJsonResponse)
        server.verify(getRequestedFor(urlEqualTo(getUrl)).withHeader("X-Pillar2-Id", equalTo(pillar2Id)))
      }

      "forward unhappy responses" in {
        given hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("X-Pillar2-Id" -> pillar2Id)

        val serverErrorResponse = Json.obj(
          "code"    -> "500",
          "message" -> "something went wrong"
        )

        stubRequestWithPillar2Id("GET", getUrl, INTERNAL_SERVER_ERROR, serverErrorResponse)

        val result = accountActivityConnector.getAccountActivity(localDateFrom, localDateTo).futureValue

        result.status mustBe INTERNAL_SERVER_ERROR
        result.body mustBe Json.stringify(serverErrorResponse)
        server.verify(getRequestedFor(urlEqualTo(getUrl)).withHeader("X-Pillar2-Id", equalTo(pillar2Id)))
      }
    }
  }
}
