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

import junit.framework.TestCase.assertEquals
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.base.UnitTestBaseSpec
import uk.gov.hmrc.pillar2submissionapi.controllers.error.{DownstreamValidationError, UnexpectedResponse}
import uk.gov.hmrc.pillar2submissionapi.helpers.ObligationsAndSubmissionsDataFixture
import uk.gov.hmrc.pillar2submissionapi.models.obligationsandsubmissions._

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class ObligationsAndSubmissionsServiceSpec extends UnitTestBaseSpec with ObligationsAndSubmissionsDataFixture {

  val obligationAndSubmissionsService: ObligationsAndSubmissionsService = new ObligationsAndSubmissionsService(mockObligationAndSubmissionsConnector)

  "obligationsAndSubmissionsService" when {
    "handleData() called with a request" should {
      "return 200 OK response" in {
        when(mockObligationAndSubmissionsConnector.getData(any[LocalDate], any[LocalDate])(using any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(HttpResponse.apply(200, Json.toJson(obligationsAndSubmissionsSuccessResponse), Map.empty)))

        val result = await(obligationAndSubmissionsService.handleData(localDateFrom, localDateTo))

        assertEquals(obligationsAndSubmissionsSuccessResponse, result)
      }
    }
  }

  "handleData() unexpected 200 response back" should {
    "Runtime exception thrown" in {
      when(mockObligationAndSubmissionsConnector.getData(any[LocalDate], any[LocalDate])(using any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse.apply(200, Json.toJson("unexpected success response"), Map.empty)))

      intercept[UnexpectedResponse.type](await(obligationAndSubmissionsService.handleData(localDateFrom, localDateTo)))
    }
  }

  "handleData() valid 422 response back" should {
    "Runtime exception thrown" in {
      when(mockObligationAndSubmissionsConnector.getData(any[LocalDate], any[LocalDate])(using any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(
          Future.successful(HttpResponse.apply(422, Json.toJson(ObligationsAndSubmissionsErrorResponse("003", "Invalid request")), Map.empty))
        )

      intercept[DownstreamValidationError](await(obligationAndSubmissionsService.handleData(localDateFrom, localDateTo)))
    }
  }

  "handleData() unexpected 422 response back" should {
    "Runtime exception thrown" in {
      when(mockObligationAndSubmissionsConnector.getData(any[LocalDate], any[LocalDate])(using any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse.apply(422, Json.toJson("unexpected error response"), Map.empty)))

      intercept[UnexpectedResponse.type](await(obligationAndSubmissionsService.handleData(localDateFrom, localDateTo)))
    }
  }

  "handleData() 500 response back" should {
    "Runtime exception thrown " in {
      when(mockObligationAndSubmissionsConnector.getData(any[LocalDate], any[LocalDate])(using any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse.apply(500, Json.toJson(InternalServerError.toString()), Map.empty)))

      intercept[UnexpectedResponse.type](await(obligationAndSubmissionsService.handleData(localDateFrom, localDateTo)))
    }
  }
}
