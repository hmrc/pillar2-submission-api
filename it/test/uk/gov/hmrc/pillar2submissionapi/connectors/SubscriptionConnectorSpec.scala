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

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalacheck.Gen
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.Application
import play.api.http.Status.OK
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.pillar2submissionapi.base.{IntegrationSpecBase, WireMockServerHandler}
import uk.gov.hmrc.pillar2submissionapi.connectors.SubscriptionConnectorSpec.{errorCodes, getSubscription}
import uk.gov.hmrc.pillar2submissionapi.helpers._

class SubscriptionConnectorSpec extends IntegrationSpecBase with WireMockServerHandler with SubscriptionLocalDataFixture {

  lazy val connector: SubscriptionConnector = app.injector.instanceOf[SubscriptionConnector]

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.pillar2.port" -> server.port()
    )
    .build()

  "SubscriptionConnector" must {
    "getSubscriptionCache" should {

      "return a Right containing SubscriptionLocalData when the backend has returned 200 OK" in {
        stubGet(s"$getSubscription/$id", OK, Json.toJson(subscriptionLocalData).toString)
        val result = connector.getSubscriptionCache(id).futureValue

        result.isRight mustBe true
        result.map(_ mustBe subscriptionLocalData)
      }

      "return a Left containing a BadRequest when the backend has returned an error" in {
        server.stubFor(
          get(urlEqualTo(s"$getSubscription/$id"))
            .willReturn(aResponse().withStatus(errorCodes.sample.value))
        )

        val result = connector.getSubscriptionCache(id).futureValue

        result.isLeft mustBe true
        result.map(_ mustBe BadRequest)
      }
    }
  }
}

object SubscriptionConnectorSpec {
  private val errorCodes: Gen[Int] = Gen.oneOf(Seq(400, 403, 500, 501, 502, 503, 504))

  private val getSubscription = "/report-pillar2-top-up-taxes/user-cache/read-subscription"
}
