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

import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, getRequestedFor, urlEqualTo}
import org.scalatest.matchers.should.Matchers.should
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsObject
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import play.api.{Application, Configuration}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.base.UnitTestBaseSpec
import uk.gov.hmrc.pillar2submissionapi.helpers.ObligationsAndSubmissionsDataFixture

class ObligationAndSubmissionsConnectorSpec extends UnitTestBaseSpec with ObligationsAndSubmissionsDataFixture {

  lazy val obligationAndSubmissionsConnector: ObligationAndSubmissionsConnector = app.injector.instanceOf[ObligationAndSubmissionsConnector]
  override def fakeApplication():             Application                       = new GuiceApplicationBuilder()
    .configure(Configuration("microservice.services.pillar2.port" -> server.port()))
    .build()

  private val getUrl = s"/report-pillar2-top-up-taxes/obligations-and-submissions/$fromDate/$toDate"

  "ObligationAndSubmissionsConnector" when {
    "getData" must {
      "forward the X-Pillar2-Id header" in {
        implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("X-Pillar2-Id" -> pillar2Id)
        stubRequestWithPillar2Id("GET", getUrl, OK, JsObject.empty)

        val result = await(obligationAndSubmissionsConnector.getData(localDateFrom, localDateTo))

        result.status should be(OK)
        server.verify(getRequestedFor(urlEqualTo(getUrl)).withHeader("X-Pillar2-Id", equalTo(pillar2Id)))
      }

      "return 200 OK for valid request" in {
        stubRequest("GET", getUrl, OK, JsObject.empty)

        val result = await(obligationAndSubmissionsConnector.getData(localDateFrom, localDateTo))

        result.status should be(OK)
      }

      "return 400 BAD_REQUEST for invalid request" in {
        stubRequest("GET", getUrl, BAD_REQUEST, JsObject.empty)

        val result = await(obligationAndSubmissionsConnector.getData(localDateFrom, localDateTo))

        result.status should be(BAD_REQUEST)
      }

      "return 404 NOT_FOUND for incorrect URL" in {
        stubRequest("GET", "/INCORRECT_URL", NOT_FOUND, JsObject.empty)

        val result = await(obligationAndSubmissionsConnector.getData(localDateFrom, localDateTo))

        result.status should be(NOT_FOUND)
      }
    }
  }
}
