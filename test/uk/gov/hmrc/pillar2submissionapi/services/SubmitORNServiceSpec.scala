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
import uk.gov.hmrc.pillar2submissionapi.models.overseasreturnnotification.{ORNSubmission, SubmitORNErrorResponse, SubmitORNSuccessResponse}
import uk.gov.hmrc.pillar2submissionapi.services.SubmitORNServiceSpec.{okResponse, validORNSubmission}

import java.time.LocalDate
import scala.concurrent.Future

class SubmitORNServiceSpec extends UnitTestBaseSpec {

  val submitORNService: SubmitORNService = new SubmitORNService(mockSubmitORNConnector)

  "submitORNService" when {
    "submitORN() called with a valid tax return" should {
      "return 201 CREATED response" in {

        when(mockSubmitORNConnector.submitORN(any[ORNSubmission])(any[HeaderCarrier]))
          .thenReturn(Future.successful(HttpResponse.apply(201, Json.toJson(okResponse), Map.empty)))

        val result = await(submitORNService.submitORN(validORNSubmission))

        assertEquals(okResponse, result)
      }
    }
  }

  "submitORN() unexpected 201 response back" should {
    "Runtime exception thrown" in {

      when(mockSubmitORNConnector.submitORN(any[ORNSubmission])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(201, Json.toJson("unexpected success response"), Map.empty)))

      intercept[UnexpectedResponse.type](await(submitORNService.submitORN(validORNSubmission)))
    }
  }

  "submitORN() valid 422 response back" should {
    "Runtime exception thrown" in {

      when(mockSubmitORNConnector.submitORN(any[ORNSubmission])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(422, Json.toJson(SubmitORNErrorResponse("093", "Invalid Return")), Map.empty)))

      intercept[ORNValidationError](await(submitORNService.submitORN(validORNSubmission)))
    }
  }

  "submitORN() unexpected 422 response back" should {
    "Runtime exception thrown" in {

      when(mockSubmitORNConnector.submitORN(any[ORNSubmission])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(422, Json.toJson("unexpected error response"), Map.empty)))

      intercept[UnexpectedResponse.type](await(submitORNService.submitORN(validORNSubmission)))
    }
  }

  "submitORN() 500 response back" should {
    "Runtime exception thrown " in {

      when(mockSubmitORNConnector.submitORN(any[ORNSubmission])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse.apply(500, Json.toJson(InternalServerError.toString()), Map.empty)))

      intercept[UnexpectedResponse.type](await(submitORNService.submitORN(validORNSubmission)))
    }
  }
}

object SubmitORNServiceSpec {
  val validORNSubmission = new ORNSubmission(
    accountingPeriodFrom = LocalDate.now(),
    accountingPeriodTo = LocalDate.now().plusYears(1),
    filedDateGIR = LocalDate.now().plusYears(1),
    countryGIR = "US",
    reportingEntityName = "Newco PLC",
    TIN = "US12345678",
    issuingCountryTIN = "US"
  )

  val okResponse: SubmitORNSuccessResponse = SubmitORNSuccessResponse("2022-01-31", "123456789012345")
}
