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

package uk.gov.hmrc.pillar2submissionapi.services

import junit.framework.TestCase.assertEquals
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.base.UnitTestBaseSpec
import uk.gov.hmrc.pillar2submissionapi.controllers.error._
import uk.gov.hmrc.pillar2submissionapi.models.belowthresholdnotification.{BTNSubmission, SubmitBTNErrorResponse, SubmitBTNSuccessResponse}
import uk.gov.hmrc.pillar2submissionapi.services.SubmitBTNServiceSpec._

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import scala.concurrent.Future

class SubmitBTNServiceSpec extends UnitTestBaseSpec {

  val submitBTNService: SubmitBTNService = new SubmitBTNService(mockSubmitBTNConnector)

  "submitBTNService" when {
    "submitBTN() called with a valid tax return" should {
      "return 201 CREATED response" in {

        when(mockSubmitBTNConnector.submitBTN(any[BTNSubmission])(using any[HeaderCarrier]))
          .thenReturn(Future.successful(HttpResponse.apply(201, Json.toJson(okResponse), Map.empty)))

        val result = await(submitBTNService.submitBTN(validBTNSubmission))

        assertEquals(okResponse, result)
      }
    }
  }

  "submitBTN() unexpected 201 response back" should {
    "Runtime exception thrown" in {

      when(mockSubmitBTNConnector.submitBTN(any[BTNSubmission])(using any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(201, Json.toJson("unexpected success response"), Map.empty)))

      intercept[UnexpectedResponse.type](await(submitBTNService.submitBTN(validBTNSubmission)))
    }
  }

  "submitBTN() valid 422 response back" should {
    "Runtime exception thrown" in {

      when(mockSubmitBTNConnector.submitBTN(any[BTNSubmission])(using any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(422, Json.toJson(SubmitBTNErrorResponse("093", "Invalid Return")), Map.empty)))

      intercept[DownstreamValidationError](await(submitBTNService.submitBTN(validBTNSubmission)))
    }
  }

  "submitBTN() unexpected 422 response back" should {
    "Runtime exception thrown" in {

      when(mockSubmitBTNConnector.submitBTN(any[BTNSubmission])(using any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(422, Json.toJson("unexpected error response"), Map.empty)))

      intercept[UnexpectedResponse.type](await(submitBTNService.submitBTN(validBTNSubmission)))
    }
  }

  "submitBTN() 500 response back" should {
    "Runtime exception thrown " in {

      when(mockSubmitBTNConnector.submitBTN(any[BTNSubmission])(using any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(500, Json.toJson(InternalServerError.toString()), Map.empty)))

      intercept[UnexpectedResponse.type](await(submitBTNService.submitBTN(validBTNSubmission)))
    }
  }
}

object SubmitBTNServiceSpec {
  val validBTNSubmission = new BTNSubmission(LocalDate.now(), LocalDate.now().plus(365, ChronoUnit.DAYS))

  val okResponse: SubmitBTNSuccessResponse = SubmitBTNSuccessResponse("2022-01-31")
}
