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

package uk.gov.hmrc.pillar2submissionapi.services

import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalacheck.Gen
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, UNAUTHORIZED}
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.base.UnitTestBaseSpec
import uk.gov.hmrc.pillar2submissionapi.connectors.AccountActivityConnector
import uk.gov.hmrc.pillar2submissionapi.controllers.error.{AccountActivityNotFound, DownstreamValidationError, UnexpectedResponse}
import uk.gov.hmrc.pillar2submissionapi.helpers.AccountActivityDataFixture

import scala.concurrent.Future

class AccountActivityServiceSpec
    extends UnitTestBaseSpec
    with AccountActivityDataFixture
    with MockitoSugar
    with ScalaFutures
    with EitherValues
    with ScalaCheckDrivenPropertyChecks {

  trait ServiceTestCase(response: HttpResponse) {
    val mockAccountActivityConnector: AccountActivityConnector = {
      val connector = mock[AccountActivityConnector]
      when(connector.getAccountActivity(eqTo(localDateFrom), eqTo(localDateTo))(using any[HeaderCarrier]))
        .thenReturn(Future.successful(response))
      connector
    }

    val accountActivityService: AccountActivityService = AccountActivityService(mockAccountActivityConnector)
  }

  "Account activity service" when {

    "parsing a 200 response" when {

      "receiving invalid JSON" must {
        "return UnexpectedResponse" in new ServiceTestCase(HttpResponse(200, "{invalid]json")) {
          accountActivityService
            .retrieveAccountActivity(localDateFrom, localDateTo)
            .value
            .futureValue
            .left
            .value mustBe UnexpectedResponse
        }
      }

      "receiving valid but unexpected json" must {
        "return UnexpectedResponse" in new ServiceTestCase(HttpResponse(200, "{}")) {
          accountActivityService
            .retrieveAccountActivity(localDateFrom, localDateTo)
            .value
            .futureValue
            .left
            .value mustBe UnexpectedResponse
        }
      }

      "receiving a valid response" must {
        "forward the json" in new ServiceTestCase(HttpResponse(200, Json.stringify(accountActivityJsonResponse))) {
          accountActivityService
            .retrieveAccountActivity(localDateFrom, localDateTo)
            .value
            .futureValue
            .value mustBe accountActivityJsonParsed

        }
      }
    }

    "parsing statuses from the schema that we don't expect to see" must {
      "return UnexpectedResponse" in forAll(Gen.oneOf(BAD_REQUEST, UNAUTHORIZED, INTERNAL_SERVER_ERROR)) { status =>
        new ServiceTestCase(HttpResponse(status)) {
          accountActivityService
            .retrieveAccountActivity(localDateFrom, localDateTo)
            .value
            .futureValue
            .left
            .value mustBe UnexpectedResponse
        }
      }
    }

    "there's no account activity to retrieve" must {
      "return AccountActivityNotFound" in new ServiceTestCase(HttpResponse(404)) {
        accountActivityService
          .retrieveAccountActivity(localDateFrom, localDateTo)
          .value
          .futureValue
          .left
          .value mustBe AccountActivityNotFound
      }
    }

    "parsing a 422" when {
      "the body is not valid json" must {
        "return UnexpectedResponse" in new ServiceTestCase(HttpResponse(422, "{invalid[json")) {
          accountActivityService
            .retrieveAccountActivity(localDateFrom, localDateTo)
            .value
            .futureValue
            .left
            .value mustBe UnexpectedResponse
        }
      }
      "the body is valid json but not in the expected shape" must {
        "return UnexpectedResponse" in new ServiceTestCase(HttpResponse(422, "{}")) {
          accountActivityService
            .retrieveAccountActivity(localDateFrom, localDateTo)
            .value
            .futureValue
            .left
            .value mustBe UnexpectedResponse
        }
      }
      "error body meets spec" must {
        "return DownstreamValidationError" in new ServiceTestCase(HttpResponse(422, "{\"code\": \"422\", \"message\": \"explanation\"}")) {
          accountActivityService
            .retrieveAccountActivity(localDateFrom, localDateTo)
            .value
            .futureValue
            .left
            .value mustBe DownstreamValidationError("422", "explanation")
        }
      }
    }

    "hitting an unexpected status" must {
      "return UnexpectedResponse" in new ServiceTestCase(HttpResponse(502)) {
        accountActivityService
          .retrieveAccountActivity(localDateFrom, localDateTo)
          .value
          .futureValue
          .left
          .value mustBe UnexpectedResponse
      }
    }
  }
}
