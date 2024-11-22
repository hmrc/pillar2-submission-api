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

//import com.github.tomakehurst.wiremock.client.WireMock._
//import org.scalacheck.Gen
//import uk.gov.hmrc.pillar2submissionapi.helpers._
//import play.api.Application
//import play.api.inject.guice.GuiceApplicationBuilder
//import play.api.libs.json.Json
//import uk.gov.hmrc.pillar2submissionapi.base.WireMockServerHandler
//import uk.gov.hmrc.pillar2submissionapi.connectors.SubscriptionConnectorSpec.{errorCodes, getSubscription, id}
//import uk.gov.hmrc.pillar2submissionapi.models.subscription.SubscriptionLocalData
//
//class SubscriptionConnectorSpec extends SubscriptionLocalDataFixture with WireMockServerHandler {
//
//  override lazy val app: Application = new GuiceApplicationBuilder()
//    .configure(
//      conf = "microservice.services.pillar2.port" -> server.port()
//    )
//    .build()
//
//  lazy val connector: SubscriptionConnector = app.injector.instanceOf[SubscriptionConnector]
//
//  "SubscriptionConnector" must {
//    "getSubscriptionCache" should {
//
//      "return Some(json) when the backend has returned 200 OK with data" in {
//        stubGet(s"$getSubscription/$id", OK, Json.toJson(emptySubscriptionLocalData).toString)
//        val result: Option[SubscriptionLocalData] = connector.getSubscriptionCache(id).futureValue
//
//        result mustBe defined
//        result mustBe Some(emptySubscriptionLocalData)
//
//      }
//
//      "return None when the backend has returned a non-success status code" in {
//        server.stubFor(
//          get(urlEqualTo(s"$getSubscription/$id"))
//            .willReturn(aResponse().withStatus(errorCodes.sample.value))
//        )
//
//        val result = connector.getSubscriptionCache(id).futureValue
//        result mustBe None
//      }
//    }
//  }
//}
//
//  object SubscriptionConnectorSpec {
//    val apiUrl = "/report-pillar2-top-up-taxes"
//    private val errorCodes: Gen[Int] = Gen.oneOf(Seq(400, 403, 500, 501, 502, 503, 504))
//
//    private val getSubscription = "/report-pillar2-top-up-taxes/user-cache/read-subscription"
//    private val id = "testId"
//}
