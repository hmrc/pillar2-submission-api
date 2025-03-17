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

package uk.gov.hmrc.pillar2submissionapi.controllers

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status.CREATED
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{OK, defaultAwaitTimeout, status}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.base.ControllerBaseSpec
import uk.gov.hmrc.pillar2submissionapi.controllers.error._
import uk.gov.hmrc.pillar2submissionapi.controllers.submission.UKTaxReturnController
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.UKTRSubmissionData

import scala.concurrent.Future

class UKTaxReturnControllerSpec extends ControllerBaseSpec {

  val uktrSubmissionController: UKTaxReturnController =
    new UKTaxReturnController(cc, identifierAction, subscriptionAction, mockUkTaxReturnService)(ec)

  def callWithBody(request: JsValue):      Future[Result] = uktrSubmissionController.submitUKTR()(FakeRequest().withJsonBody(request))
  def callAmendWithBody(request: JsValue): Future[Result] = uktrSubmissionController.amendUKTR()(FakeRequest().withJsonBody(request))

  "UktrSubmissionController" when {
    "submitUKTR() called with a valid request" should {
      "return 201 CREATED response" in {
        when(mockUkTaxReturnService.submitUKTR(any[UKTRSubmissionData])(any[HeaderCarrier]))
          .thenReturn(Future.successful(uktrSubmissionSuccessResponse))

        status(of = callWithBody(validLiabilityReturn)) mustEqual CREATED
      }

      "forward the X-Pillar2-Id header" in {
        val captor = ArgumentCaptor.forClass(classOf[HeaderCarrier])
        when(mockUkTaxReturnService.submitUKTR(any[UKTRSubmissionData])(captor.capture()))
          .thenReturn(Future.successful(uktrSubmissionSuccessResponse))

        status(of = callWithBody(validLiabilityReturn)) mustEqual CREATED
        captor.getValue.extraHeaders.map(_._1) must contain("X-Pillar2-Id")
      }
    }

    "submitUKTR() called with a valid nil return request" should {
      "return 201 CREATED response" in {
        when(mockUkTaxReturnService.submitUKTR(any[UKTRSubmissionData])(any[HeaderCarrier]))
          .thenReturn(Future.successful(uktrSubmissionSuccessResponse))

        status(of = callWithBody(validNilReturn)) mustEqual CREATED
      }

      "forward the X-Pillar2-Id header" in {
        val captor = ArgumentCaptor.forClass(classOf[HeaderCarrier])
        when(mockUkTaxReturnService.submitUKTR(any[UKTRSubmissionData])(captor.capture()))
          .thenReturn(Future.successful(uktrSubmissionSuccessResponse))

        status(of = callWithBody(validNilReturn)) mustEqual CREATED
        captor.getValue.extraHeaders.map(_._1) must contain("X-Pillar2-Id")
      }
    }

    "submitUKTR() called with an invalid request" should {
      "return 400 BAD_REQUEST response" in {
        callWithBody(liabilityReturnInvalidLiabilities) shouldFailWith InvalidJson
      }
    }

    "submitUKTR() called with an invalid nil return request" should {
      "return 400 BAD_REQUEST response" in {

        callWithBody(nilReturnInvalidReturnType) shouldFailWith InvalidJson
      }
    }

    "submitUKTR() called with an invalid json request" should {
      "return 400 BAD_REQUEST response" in {
        callWithBody(invalidBody) shouldFailWith InvalidJson
      }
    }

    "submitUKTR() called with request that only contains a valid return type" should {
      "return 400 BAD_REQUEST response" in {

        callWithBody(invalidRequest_nilReturn_onlyContainsLiabilities) shouldFailWith InvalidJson
      }
    }

    "submitUKTR() called with request that only contains an invalid return type" should {
      "return 400 BAD_REQUEST response" in {
        callWithBody(invalidRequest_nilReturn_onlyLiabilitiesButInvalidReturnType) shouldFailWith InvalidJson
      }
    }

    "submitUKTR() called with request that is missing liabilities" should {
      "return 400 BAD_REQUEST response" in {
        callWithBody(invalidRequest_noLiabilities) shouldFailWith InvalidJson
      }
    }

    "submitUKTR() called with an empty json object" should {
      "return 400 BAD_REQUEST response" in {
        callWithBody(emptyBody) shouldFailWith InvalidJson
      }
    }

    "submitUKTR() called with an non-json request" should {
      "return 400 BAD_REQUEST response" in {
        val result = uktrSubmissionController.submitUKTR()(FakeRequest().withTextBody(stringBody))

        result shouldFailWith EmptyRequestBody
      }
    }

    "submitUKTR() called with no request body" should {
      "return 400 BAD_REQUEST response" in {
        val result = uktrSubmissionController.submitUKTR()(FakeRequest())

        result shouldFailWith EmptyRequestBody
      }
    }

    "submitUKTR() called with valid request body that contains duplicate entries" should {
      "return 201 CREATED response" in {
        status(of = callWithBody(liabilityReturnDuplicateFields)) mustEqual CREATED
      }
    }

    "submitUKTR() called with valid request body that contains additional fields" should {
      "return 201 CREATED response" in {
        status(of = callWithBody(liabilityReturnWithadditionalFields)) mustEqual CREATED
      }
    }

    "submitUKTR() called with valid request body that contains both a full and a nil submission" should {
      "return 201 CREATED response" in {
        status(of = callWithBody(liabilityAndNilReturn)) mustEqual CREATED
      }
    }

    "submitUKTR() called with a monetary value exceeding the maximum allowed" should {
      "return 400 BAD_REQUEST response with MonetaryValueExceedsLimit" in {

        val invalidMonetaryValueRequest = Json.parse("""{
            |  "accountingPeriodFrom": "2024-08-14",
            |  "accountingPeriodTo": "2024-12-14",
            |  "obligationMTT": true,
            |  "electionUKGAAP": true,
            |  "liabilities": {
            |    "electionDTTSingleMember": false,
            |    "electionUTPRSingleMember": false,
            |    "numberSubGroupDTT": 1,
            |    "numberSubGroupUTPR": 1,
            |    "totalLiability": 10000000000000.00,
            |    "totalLiabilityDTT": 5000.99,
            |    "totalLiabilityIIR": 4000,
            |    "totalLiabilityUTPR": 10000.99,
            |    "liableEntities": [
            |      {
            |        "ukChargeableEntityName": "Newco PLC",
            |        "idType": "CRN",
            |        "idValue": "12345678",
            |        "amountOwedDTT": 5000,
            |        "amountOwedIIR": 3400,
            |        "amountOwedUTPR": 6000.5
            |      }
            |    ]
            |  }
            |}""".stripMargin)

        callWithBody(invalidMonetaryValueRequest) shouldFailWith MonetaryValueExceedsLimit
      }
    }

    "submitUKTR() called with a monetary value having too many decimal places" should {
      "return 400 BAD_REQUEST response with MonetaryDecimalPrecisionError" in {

        val invalidDecimalsRequest = Json.parse("""{
            |  "accountingPeriodFrom": "2024-08-14",
            |  "accountingPeriodTo": "2024-12-14",
            |  "obligationMTT": true,
            |  "electionUKGAAP": true,
            |  "liabilities": {
            |    "electionDTTSingleMember": false,
            |    "electionUTPRSingleMember": false,
            |    "numberSubGroupDTT": 1,
            |    "numberSubGroupUTPR": 1,
            |    "totalLiability": 10000.999,
            |    "totalLiabilityDTT": 5000.99,
            |    "totalLiabilityIIR": 4000,
            |    "totalLiabilityUTPR": 10000.99,
            |    "liableEntities": [
            |      {
            |        "ukChargeableEntityName": "Newco PLC",
            |        "idType": "CRN",
            |        "idValue": "12345678",
            |        "amountOwedDTT": 5000,
            |        "amountOwedIIR": 3400,
            |        "amountOwedUTPR": 6000.5
            |      }
            |    ]
            |  }
            |}""".stripMargin)

        callWithBody(invalidDecimalsRequest) shouldFailWith MonetaryDecimalPrecisionError
      }
    }

    "submitUKTR() called with an invalid monetary value in liableEntity amountOwedDTT" should {
      "return 400 BAD_REQUEST response with MonetaryValueExceedsLimit" in {
        val requestWithInvalidEntityAmount = Json.parse("""{
            |  "accountingPeriodFrom": "2024-08-14",
            |  "accountingPeriodTo": "2024-12-14",
            |  "obligationMTT": true,
            |  "electionUKGAAP": true,
            |  "liabilities": {
            |    "electionDTTSingleMember": false,
            |    "electionUTPRSingleMember": false,
            |    "numberSubGroupDTT": 1,
            |    "numberSubGroupUTPR": 1,
            |    "totalLiability": 10000.99,
            |    "totalLiabilityDTT": 5000.99,
            |    "totalLiabilityIIR": 4000,
            |    "totalLiabilityUTPR": 10000.99,
            |    "liableEntities": [
            |      {
            |        "ukChargeableEntityName": "Newco PLC",
            |        "idType": "CRN",
            |        "idValue": "12345678",
            |        "amountOwedDTT": 10000000000000.00,
            |        "amountOwedIIR": 3400,
            |        "amountOwedUTPR": 6000.5
            |      }
            |    ]
            |  }
            |}""".stripMargin)

        callWithBody(requestWithInvalidEntityAmount) shouldFailWith MonetaryValueExceedsLimit
      }
    }

    "submitUKTR() called with a monetary value with too many decimal places in liableEntity amountOwedIIR" should {
      "return 400 BAD_REQUEST response with MonetaryDecimalPrecisionError" in {
        val requestWithInvalidEntityDecimals = Json.parse("""{
            |  "accountingPeriodFrom": "2024-08-14",
            |  "accountingPeriodTo": "2024-12-14",
            |  "obligationMTT": true,
            |  "electionUKGAAP": true,
            |  "liabilities": {
            |    "electionDTTSingleMember": false,
            |    "electionUTPRSingleMember": false,
            |    "numberSubGroupDTT": 1,
            |    "numberSubGroupUTPR": 1,
            |    "totalLiability": 10000.99,
            |    "totalLiabilityDTT": 5000.99,
            |    "totalLiabilityIIR": 4000,
            |    "totalLiabilityUTPR": 10000.99,
            |    "liableEntities": [
            |      {
            |        "ukChargeableEntityName": "Newco PLC",
            |        "idType": "CRN",
            |        "idValue": "12345678",
            |        "amountOwedDTT": 5000.00,
            |        "amountOwedIIR": 3400.999,
            |        "amountOwedUTPR": 6000.5
            |      }
            |    ]
            |  }
            |}""".stripMargin)

        callWithBody(requestWithInvalidEntityDecimals) shouldFailWith MonetaryDecimalPrecisionError
      }
    }

    "submitUKTR() called with a negative monetary value at the minimum limit" should {
      "return 201 CREATED response" in {
        when(mockUkTaxReturnService.submitUKTR(any[UKTRSubmissionData])(any[HeaderCarrier]))
          .thenReturn(Future.successful(uktrSubmissionSuccessResponse))

        val requestWithMinimumMonetaryValue = Json.parse("""{
            |  "accountingPeriodFrom": "2024-08-14",
            |  "accountingPeriodTo": "2024-12-14",
            |  "obligationMTT": true,
            |  "electionUKGAAP": true,
            |  "liabilities": {
            |    "electionDTTSingleMember": false,
            |    "electionUTPRSingleMember": false,
            |    "numberSubGroupDTT": 1,
            |    "numberSubGroupUTPR": 1,
            |    "totalLiability": -9999999999999.99,
            |    "totalLiabilityDTT": 5000.99,
            |    "totalLiabilityIIR": 4000,
            |    "totalLiabilityUTPR": 10000.99,
            |    "liableEntities": [
            |      {
            |        "ukChargeableEntityName": "Newco PLC",
            |        "idType": "CRN",
            |        "idValue": "12345678",
            |        "amountOwedDTT": 5000.00,
            |        "amountOwedIIR": 3400.00,
            |        "amountOwedUTPR": -9999999999999.99
            |      }
            |    ]
            |  }
            |}""".stripMargin)

        status(of = callWithBody(requestWithMinimumMonetaryValue)) mustEqual CREATED
      }
    }

    "amendUKTR() called with a valid request" should {
      "return 200 OK response" in {
        when(mockUkTaxReturnService.amendUKTR(any[UKTRSubmissionData])(any[HeaderCarrier]))
          .thenReturn(Future.successful(uktrSubmissionSuccessResponse))

        status(of = callAmendWithBody(validLiabilityReturn)) mustEqual OK
      }

      "forward the X-Pillar2-Id header" in {
        val captor = ArgumentCaptor.forClass(classOf[HeaderCarrier])
        when(mockUkTaxReturnService.amendUKTR(any[UKTRSubmissionData])(captor.capture()))
          .thenReturn(Future.successful(uktrSubmissionSuccessResponse))

        status(of = callAmendWithBody(validLiabilityReturn)) mustEqual OK
        captor.getValue.extraHeaders.map(_._1) must contain("X-Pillar2-Id")
      }
    }

    "amendUKTR() called with a valid nil return request" should {
      "return 200 OK response" in {
        when(mockUkTaxReturnService.amendUKTR(any[UKTRSubmissionData])(any[HeaderCarrier]))
          .thenReturn(Future.successful(uktrSubmissionSuccessResponse))

        status(of = callAmendWithBody(validNilReturn)) mustEqual OK
      }

      "forward the X-Pillar2-Id header" in {
        val captor = ArgumentCaptor.forClass(classOf[HeaderCarrier])
        when(mockUkTaxReturnService.amendUKTR(any[UKTRSubmissionData])(captor.capture()))
          .thenReturn(Future.successful(uktrSubmissionSuccessResponse))

        status(of = callAmendWithBody(validNilReturn)) mustEqual OK
        captor.getValue.extraHeaders.map(_._1) must contain("X-Pillar2-Id")
      }
    }

    "amendUKTR() called with an invalid request" should {
      "return 400 BAD_REQUEST response" in {
        callAmendWithBody(liabilityReturnInvalidLiabilities) shouldFailWith InvalidJson
      }
    }

    "amendUKTR() called with an invalid nil return request" should {
      "return 400 BAD_REQUEST response" in {
        callAmendWithBody(nilReturnInvalidReturnType) shouldFailWith InvalidJson
      }
    }

    "amendUKTR() called with an invalid json request" should {
      "return 400 BAD_REQUEST response" in {
        callAmendWithBody(invalidBody) shouldFailWith InvalidJson
      }
    }

    "amendUKTR() called with request that only contains a valid return type" should {
      "return 400 BAD_REQUEST response" in {
        callAmendWithBody(invalidRequest_nilReturn_onlyContainsLiabilities) shouldFailWith InvalidJson
      }
    }

    "amendUKTR() called with request that only contains an invalid return type" should {
      "return 400 BAD_REQUEST response" in {
        callAmendWithBody(invalidRequest_nilReturn_onlyLiabilitiesButInvalidReturnType) shouldFailWith InvalidJson
      }
    }

    "amendUKTR() called with request that is missing liabilities" should {
      "return 400 BAD_REQUEST response" in {
        callAmendWithBody(invalidRequest_noLiabilities) shouldFailWith InvalidJson
      }
    }

    "amendUKTR() called with an empty json object" should {
      "return 400 BAD_REQUEST response" in {
        callAmendWithBody(emptyBody) shouldFailWith InvalidJson
      }
    }

    "amendUKTR() called with an non-json request" should {
      "return 400 BAD_REQUEST response" in {
        val result = uktrSubmissionController.amendUKTR()(FakeRequest().withTextBody(stringBody))

        result shouldFailWith EmptyRequestBody
      }
    }

    "amendUKTR() called with no request body" should {
      "return 400 BAD_REQUEST response" in {
        val result = uktrSubmissionController.amendUKTR()(FakeRequest())

        result shouldFailWith EmptyRequestBody
      }
    }

    "amendUKTR() called with valid request body that contains duplicate entries" should {
      "return 200 OK response" in {
        status(of = callAmendWithBody(liabilityReturnDuplicateFields)) mustEqual OK
      }
    }

    "amendUKTR() called with valid request body that contains additional fields" should {
      "return 200 OK response" in {
        status(of = callAmendWithBody(liabilityReturnWithadditionalFields)) mustEqual OK
      }
    }

    "amendUKTR() called with valid request body that contains both a full and a nil submission" should {
      "return 200 OK response" in {
        status(of = callAmendWithBody(liabilityAndNilReturn)) mustEqual OK
      }
    }

    "amendUKTR() called with a monetary value exceeding the maximum allowed" should {
      "return 400 BAD_REQUEST response with MonetaryValueExceedsLimit" in {

        val invalidMonetaryValueRequest = Json.parse("""{
            |  "accountingPeriodFrom": "2024-08-14",
            |  "accountingPeriodTo": "2024-12-14",
            |  "obligationMTT": true,
            |  "electionUKGAAP": true,
            |  "liabilities": {
            |    "electionDTTSingleMember": false,
            |    "electionUTPRSingleMember": false,
            |    "numberSubGroupDTT": 1,
            |    "numberSubGroupUTPR": 1,
            |    "totalLiability": 10000000000000.00,
            |    "totalLiabilityDTT": 5000.99,
            |    "totalLiabilityIIR": 4000,
            |    "totalLiabilityUTPR": 10000.99,
            |    "liableEntities": [
            |      {
            |        "ukChargeableEntityName": "Newco PLC",
            |        "idType": "CRN",
            |        "idValue": "12345678",
            |        "amountOwedDTT": 5000,
            |        "amountOwedIIR": 3400,
            |        "amountOwedUTPR": 6000.5
            |      }
            |    ]
            |  }
            |}""".stripMargin)

        callAmendWithBody(invalidMonetaryValueRequest) shouldFailWith MonetaryValueExceedsLimit
      }
    }

    "amendUKTR() called with a monetary value having too many decimal places" should {
      "return 400 BAD_REQUEST response with MonetaryDecimalPrecisionError" in {
        // Create test data with too many decimal places
        val invalidDecimalsRequest = Json.parse("""{
            |  "accountingPeriodFrom": "2024-08-14",
            |  "accountingPeriodTo": "2024-12-14",
            |  "obligationMTT": true,
            |  "electionUKGAAP": true,
            |  "liabilities": {
            |    "electionDTTSingleMember": false,
            |    "electionUTPRSingleMember": false,
            |    "numberSubGroupDTT": 1,
            |    "numberSubGroupUTPR": 1,
            |    "totalLiability": 10000.999,
            |    "totalLiabilityDTT": 5000.99,
            |    "totalLiabilityIIR": 4000,
            |    "totalLiabilityUTPR": 10000.99,
            |    "liableEntities": [
            |      {
            |        "ukChargeableEntityName": "Newco PLC",
            |        "idType": "CRN",
            |        "idValue": "12345678",
            |        "amountOwedDTT": 5000,
            |        "amountOwedIIR": 3400,
            |        "amountOwedUTPR": 6000.5
            |      }
            |    ]
            |  }
            |}""".stripMargin)

        callAmendWithBody(invalidDecimalsRequest) shouldFailWith MonetaryDecimalPrecisionError
      }
    }

    "amendUKTR() called with an invalid monetary value in liableEntity amountOwedDTT" should {
      "return 400 BAD_REQUEST response with MonetaryValueExceedsLimit" in {
        val requestWithInvalidEntityAmount = Json.parse("""{
            |  "accountingPeriodFrom": "2024-08-14",
            |  "accountingPeriodTo": "2024-12-14",
            |  "obligationMTT": true,
            |  "electionUKGAAP": true,
            |  "liabilities": {
            |    "electionDTTSingleMember": false,
            |    "electionUTPRSingleMember": false,
            |    "numberSubGroupDTT": 1,
            |    "numberSubGroupUTPR": 1,
            |    "totalLiability": 10000.99,
            |    "totalLiabilityDTT": 5000.99,
            |    "totalLiabilityIIR": 4000,
            |    "totalLiabilityUTPR": 10000.99,
            |    "liableEntities": [
            |      {
            |        "ukChargeableEntityName": "Newco PLC",
            |        "idType": "CRN",
            |        "idValue": "12345678",
            |        "amountOwedDTT": 10000000000000.00,
            |        "amountOwedIIR": 3400,
            |        "amountOwedUTPR": 6000.5
            |      }
            |    ]
            |  }
            |}""".stripMargin)

        callAmendWithBody(requestWithInvalidEntityAmount) shouldFailWith MonetaryValueExceedsLimit
      }
    }

    "amendUKTR() called with a monetary value with too many decimal places in liableEntity amountOwedIIR" should {
      "return 400 BAD_REQUEST response with MonetaryDecimalPrecisionError" in {
        val requestWithInvalidEntityDecimals = Json.parse("""{
            |  "accountingPeriodFrom": "2024-08-14",
            |  "accountingPeriodTo": "2024-12-14",
            |  "obligationMTT": true,
            |  "electionUKGAAP": true,
            |  "liabilities": {
            |    "electionDTTSingleMember": false,
            |    "electionUTPRSingleMember": false,
            |    "numberSubGroupDTT": 1,
            |    "numberSubGroupUTPR": 1,
            |    "totalLiability": 10000.99,
            |    "totalLiabilityDTT": 5000.99,
            |    "totalLiabilityIIR": 4000,
            |    "totalLiabilityUTPR": 10000.99,
            |    "liableEntities": [
            |      {
            |        "ukChargeableEntityName": "Newco PLC",
            |        "idType": "CRN",
            |        "idValue": "12345678",
            |        "amountOwedDTT": 5000.00,
            |        "amountOwedIIR": 3400.999,
            |        "amountOwedUTPR": 6000.5
            |      }
            |    ]
            |  }
            |}""".stripMargin)

        callAmendWithBody(requestWithInvalidEntityDecimals) shouldFailWith MonetaryDecimalPrecisionError
      }
    }

    "amendUKTR() called with a negative monetary value at the minimum limit" should {
      "return 200 OK response" in {
        when(mockUkTaxReturnService.amendUKTR(any[UKTRSubmissionData])(any[HeaderCarrier]))
          .thenReturn(Future.successful(uktrSubmissionSuccessResponse))

        val requestWithMinimumMonetaryValue = Json.parse("""{
            |  "accountingPeriodFrom": "2024-08-14",
            |  "accountingPeriodTo": "2024-12-14",
            |  "obligationMTT": true,
            |  "electionUKGAAP": true,
            |  "liabilities": {
            |    "electionDTTSingleMember": false,
            |    "electionUTPRSingleMember": false,
            |    "numberSubGroupDTT": 1,
            |    "numberSubGroupUTPR": 1,
            |    "totalLiability": -9999999999999.99,
            |    "totalLiabilityDTT": 5000.99,
            |    "totalLiabilityIIR": 4000,
            |    "totalLiabilityUTPR": 10000.99,
            |    "liableEntities": [
            |      {
            |        "ukChargeableEntityName": "Newco PLC",
            |        "idType": "CRN",
            |        "idValue": "12345678",
            |        "amountOwedDTT": 5000.00,
            |        "amountOwedIIR": 3400.00,
            |        "amountOwedUTPR": -9999999999999.99
            |      }
            |    ]
            |  }
            |}""".stripMargin)

        status(of = callAmendWithBody(requestWithMinimumMonetaryValue)) mustEqual OK
      }
    }
  }
}
