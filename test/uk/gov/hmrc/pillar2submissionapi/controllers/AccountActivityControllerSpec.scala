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

package uk.gov.hmrc.pillar2submissionapi.controllers

import cats.data.EitherT
import cats.syntax.either.given
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import play.api.http.Status.OK
import play.api.mvc.AnyContentAsEmpty
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, status, given}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.base.ControllerBaseSpec
import uk.gov.hmrc.pillar2submissionapi.controllers.accountactivity.AccountActivityController
import uk.gov.hmrc.pillar2submissionapi.controllers.error._
import uk.gov.hmrc.pillar2submissionapi.helpers.AccountActivityDataFixture
import uk.gov.hmrc.pillar2submissionapi.models.accountactivity.AccountActivitySuccessResponse
import uk.gov.hmrc.pillar2submissionapi.services.AccountActivityService

import scala.concurrent.Future

class AccountActivityControllerSpec extends ControllerBaseSpec with AccountActivityDataFixture {

  trait TestCase(result: Either[Pillar2Error, AccountActivitySuccessResponse]) {
    val mockAccountActivityService: AccountActivityService = {
      val aaService = mock[AccountActivityService]
      when(aaService.retrieveAccountActivity(eqTo(localDateFrom), eqTo(localDateTo))(using any[HeaderCarrier]))
        .thenReturn(EitherT.fromEither[Future](result))
      aaService
    }

    val controller: AccountActivityController = AccountActivityController(mockAccountActivityService, identifierAction, pillar2IdAction, cc)

    val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders("X-Pillar2-Id" -> pillar2Id)
  }

  "retrieveAccountActivity" must {
    "return OK with the expected response on the happy path" in new TestCase(accountActivityJsonParsed.asRight) {
      val result: Future[Result] = controller.retrieveAccountActivity(fromDate, toDate)(request)
      status(result) mustBe OK
      contentAsJson(result) mustBe accountActivityJsonResponse
    }

    "fail with InvalidDateFormat when either of the query parameters can't be parsed" in new TestCase(accountActivityJsonParsed.asRight) {
      val badFirstDate:  Future[Result] = controller.retrieveAccountActivity("not-a-date", toDate)(request)
      val badSecondDate: Future[Result] = controller.retrieveAccountActivity(fromDate, "not-a-date")(request)

      badFirstDate.shouldFailWith(InvalidDateFormat)
      badSecondDate.shouldFailWith(InvalidDateFormat)
    }

    "fail with InvalidDateRange when from is after to" in new TestCase(accountActivityJsonParsed.asRight) {
      controller
        .retrieveAccountActivity(localDateFrom.toString, localDateFrom.minusDays(1).toString)(request)
        .shouldFailWith(InvalidDateRange)
    }

    "return MissingHeader when X-Pillar2-Id header not provided" in new TestCase(accountActivityJsonParsed.asRight) {
      controller
        .retrieveAccountActivity(fromDate, toDate)(FakeRequest())
        .shouldFailWith(MissingHeader.MissingPillar2Id)
    }

    "return the cause when an upstream layer fails in the Future's error channel" in new TestCase(accountActivityJsonParsed.asRight) {
      when(mockAccountActivityService.retrieveAccountActivity(eqTo(localDateFrom), eqTo(localDateTo))(using any[HeaderCarrier]))
        .thenReturn(EitherT.liftF(Future.failed(UnexpectedResponse)))

      controller
        .retrieveAccountActivity(fromDate, toDate)(request)
        .shouldFailWith(UnexpectedResponse)
    }

    "return the cause when an upstream layer fails in the Either error channel" in new TestCase(UnexpectedResponse.asLeft) {
      controller
        .retrieveAccountActivity(fromDate, toDate)(request)
        .shouldFailWith(UnexpectedResponse)
    }
  }
}
