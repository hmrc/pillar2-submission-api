/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.pillar2submissionapi.helpers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}
import play.api.http.HttpVerbs._
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HeaderCarrier

trait WireMockServerHandler extends BeforeAndAfterAll with BeforeAndAfterEach {
  this: Suite =>

  val wiremockPort: Int = 11111

  protected val server: WireMockServer = new WireMockServer(
    wireMockConfig.port(wiremockPort)
  )

  override def beforeAll(): Unit = {
    server.start()
    super.beforeAll()
  }

  override def beforeEach(): Unit = {
    server.resetAll()
    super.beforeEach()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    server.stop()
  }

  protected def stubGet(expectedEndpoint: String, expectedStatus: Int, expectedBody: String): StubMapping =
    server.stubFor(
      get(urlEqualTo(s"$expectedEndpoint"))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
            .withBody(expectedBody)
        )
    )

  protected def stubRequest(
    method:         String,
    expectedUrl:    String,
    expectedStatus: Int,
    body:           JsValue,
    headers:        Map[String, String] = Map.empty
  ): StubMapping = {
    val methodMapping = method.toUpperCase match {
      case GET    => get(urlEqualTo(expectedUrl))
      case POST   => post(urlEqualTo(expectedUrl))
      case PUT    => put(urlEqualTo(expectedUrl))
      case DELETE => delete(urlEqualTo(expectedUrl))
      case _      => throw new IllegalArgumentException(s"Unsupported HTTP method: $method")
    }

    val mappingWithHeaders = headers.foldLeft(methodMapping) { case (mapping, (key, value)) =>
      mapping.withHeader(key, equalTo(value))
    }

    server.stubFor(
      mappingWithHeaders
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
            .withBody(body.toString())
        )
    )
  }

  protected def stubRequestWithPillar2Id(
    method:         String,
    expectedUrl:    String,
    expectedStatus: Int,
    body:           JsValue
  )(implicit hc: HeaderCarrier): StubMapping = {
    val pillar2IdHeader = hc.extraHeaders.find(_._1 == "X-Pillar2-Id").getOrElse(throw new IllegalArgumentException("X-Pillar2-Id header not found"))
    stubRequest(method, expectedUrl, expectedStatus, body, Map(pillar2IdHeader))
  }
}
