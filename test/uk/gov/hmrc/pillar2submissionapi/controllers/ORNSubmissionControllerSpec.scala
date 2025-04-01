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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status.CREATED
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.base.ControllerBaseSpec
import uk.gov.hmrc.pillar2submissionapi.controllers.ORNSubmissionControllerSpec._
import uk.gov.hmrc.pillar2submissionapi.controllers.error.{EmptyRequestBody, InvalidJson}
import uk.gov.hmrc.pillar2submissionapi.controllers.submission.ORNSubmissionController
import uk.gov.hmrc.pillar2submissionapi.models.overseasreturnnotification.{ORNSubmission, SubmitORNSuccessResponse}

import scala.concurrent.Future

class ORNSubmissionControllerSpec extends ControllerBaseSpec {

  val ORNSubmissionController: ORNSubmissionController =
    new ORNSubmissionController(cc, identifierAction, subscriptionDataRetrievalAction, mockSubmitORNService)

  def result(jsRequest: JsValue): Future[Result] = ORNSubmissionController.submitORN(
    FakeRequest()
      .withJsonBody(jsRequest)
  )

  "ORNSubmissionController" when {
    "submitORN() called with a valid request" should {
      "return 201 CREATED response" in {

        when(mockSubmitORNService.submitORN(any[ORNSubmission])(any[HeaderCarrier]))
          .thenReturn(
            Future.successful(
              SubmitORNSuccessResponse("2022-01-31T09:26:17Z", "123456789012345")
            )
          )

        status(result(validRequestJson_data)) mustEqual CREATED
      }
    }

    "submitORN called with an invalid request" should {
      "return InvalidJson response" in {

        result(invalidRequestJson_data) shouldFailWith InvalidJson
      }
    }

    "submitORN called with an invalid json request" should {
      "return InvalidJson response" in {

        result(invalidRequest_Json) shouldFailWith InvalidJson
      }
    }

    "submitORN called with an empty json object" should {
      "return InvalidJson response" in {

        result(invalidRequest_emptyBody) shouldFailWith InvalidJson
      }
    }

    "submitORN called with an non-json request" should {
      "return EmptyRequestBody response" in {
        val result: Future[Result] = ORNSubmissionController.submitORN(
          FakeRequest()
            .withTextBody(invalidRequest_wrongType)
        )
        result shouldFailWith EmptyRequestBody
      }
    }

    "submitORN called with no request body" should {
      "return EmptyRequestBody response" in {
        val result: Future[Result] = ORNSubmissionController.submitORN(
          FakeRequest()
        )
        result shouldFailWith EmptyRequestBody
      }
    }

    "submitORN called with valid request body that contains duplicate entries" should {
      "return 201 CREATED response" in {

        status(result(validRequestJson_duplicateFields)) mustEqual CREATED
      }
    }

    "submitORN called with valid request body that contains additional fields" should {
      "return 201 CREATED response" in {

        status(result(validRequestJson_additionalFields)) mustEqual CREATED
      }
    }
  }
}

object ORNSubmissionControllerSpec {
  val validRequestJson_data: JsValue =
    Json.parse("""{
        |  "accountingPeriodFrom": "2023-01-01",
        |  "accountingPeriodTo": "2024-12-31",
        |  "filedDateGIR": "2024-12-31",
        |  "countryGIR":"US",
        |  "reportingEntityName" : "Newco PLC",
        |  "TIN" : "US12345678",
        |  "issuingCountryTIN" : "US"
        |}""".stripMargin)

  val invalidRequestJson_data: JsValue =
    Json.parse("""{
        |  "data1": "value1",
        |  "data2": "value2"
        |}""".stripMargin)

  val invalidRequest_Json: JsValue =
    Json.parse("""{
        |  "badRequest": ""
        |}""".stripMargin)

  val invalidRequest_emptyBody: JsValue = JsObject.empty

  val invalidRequest_wrongType: String = "This is not Json."

  val validRequestJson_duplicateFields: JsValue =
    Json.parse("""{
                 |  "accountingPeriodFrom": "2023-01-01",
                 |  "accountingPeriodTo": "2024-12-31",
                 |  "accountingPeriodFrom": "2023-01-01",
                 |  "accountingPeriodTo": "2024-12-31",
                 |  "filedDateGIR": "2024-12-31",
                 |  "countryGIR":"US",
                 |  "reportingEntityName" : "Newco PLC",
                 |  "TIN" : "US12345678",
                 |  "issuingCountryTIN" : "US"
                 |}""".stripMargin)

  val validRequestJson_additionalFields: JsValue =
    Json.parse("""{
                 |  "accountingPeriodFrom": "2023-01-01",
                 |  "accountingPeriodTo": "2024-12-31",
                 |  "extraField1": "value1",
                 |  "extraField1": "value2",
                 |  "filedDateGIR": "2024-12-31",
                 |  "countryGIR":"US",
                 |  "reportingEntityName" : "Newco PLC",
                 |  "TIN" : "US12345678",
                 |  "issuingCountryTIN" : "US"
                 |}""".stripMargin)

}
