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
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.base.ControllerBaseSpec
import uk.gov.hmrc.pillar2submissionapi.controllers.error.{EmptyRequestBody, InvalidJson, MissingHeader}
import uk.gov.hmrc.pillar2submissionapi.controllers.submission.ORNSubmissionController
import uk.gov.hmrc.pillar2submissionapi.helpers.ORNDataFixture
import uk.gov.hmrc.pillar2submissionapi.models.overseasreturnnotification.{ORNSubmission, ORNSuccessResponse}

import scala.concurrent.Future

class ORNSubmissionControllerSpec extends ControllerBaseSpec with ORNDataFixture {

  val ORNSubmissionController: ORNSubmissionController =
    new ORNSubmissionController(cc, identifierAction, pillar2IdAction, subscriptionDataRetrievalAction, mockSubmitORNService)

  def result(jsRequest: JsValue): Future[Result] = ORNSubmissionController.submitORN(
    FakeRequest()
      .withJsonBody(jsRequest)
      .withHeaders("X-Pillar2-Id" -> pillar2Id)
  )

  "ORNSubmissionController" when {
    "submitORN() called with a valid request" should {
      "return 201 CREATED response" in {

        when(mockSubmitORNService.submitORN(any[ORNSubmission])(any[HeaderCarrier]))
          .thenReturn(
            Future.successful(
              ORNSuccessResponse("2022-01-31T09:26:17Z", "123456789012345")
            )
          )

        status(result(ornRequestJs)) mustEqual CREATED
      }
    }

    "submitORN called with an invalid request" should {
      "return InvalidJson response" in {
        val invalidRequestJson_data: JsValue = ornRequestJs.as[JsObject] - "filedDateGIR" - "TIN" // Remove fields to make the JSON invalid

        result(invalidRequestJson_data) shouldFailWith InvalidJson
      }
    }

    "submitORN called with an invalid json request" should {
      "return InvalidJson response" in {

        val invalidRequest_Json: JsValue =
          ornRequestJs.as[JsObject] + ("accountingPeriodFrom" -> JsString("invalid-date"))

        result(invalidRequest_Json) shouldFailWith InvalidJson
      }
    }

    "submitORN called with an empty json object" should {
      "return InvalidJson response" in {
        val invalidRequest_emptyBody: JsValue = JsObject.empty

        result(invalidRequest_emptyBody) shouldFailWith InvalidJson
      }
    }

    "submitORN called without X-Pillar2-Id" should {
      "return MissingHeader response" in {
        ORNSubmissionController.submitORN(
          FakeRequest()
        ) shouldFailWith MissingHeader.MissingPillar2Id
      }
    }

    "submitORN called with an non-json request" should {
      "return EmptyRequestBody response" in {
        val invalidRequest_wrongType: String = "This is not Json."
        val result: Future[Result] = ORNSubmissionController.submitORN(
          FakeRequest()
            .withTextBody(invalidRequest_wrongType)
            .withHeaders("X-Pillar2-Id" -> pillar2Id)
        )
        result shouldFailWith EmptyRequestBody
      }
    }

    "submitORN called with no request body" should {
      "return EmptyRequestBody response" in {
        val result: Future[Result] = ORNSubmissionController.submitORN(
          FakeRequest().withHeaders("X-Pillar2-Id" -> pillar2Id)
        )
        result shouldFailWith EmptyRequestBody
      }
    }

    "submitORN called with valid request body that contains duplicate entries" should {
      "return 201 CREATED response" in {
        val validRequestJson_duplicateFields: JsValue =
          ornRequestJs.as[JsObject] + ("accountingPeriodFrom" -> JsString("2023-01-01"))

        status(result(validRequestJson_duplicateFields)) mustEqual CREATED
      }
    }

    "submitORN called with valid request body that contains additional fields" should {
      "return 201 CREATED response" in {

        val validRequestJson_additionalFields: JsValue =
          ornRequestJs.as[JsObject] + ("extraField" -> JsString("extraValue"))

        status(result(validRequestJson_additionalFields)) mustEqual CREATED
      }
    }

    "submitORN called with invalid field lengths" should {

      "return InvalidJson response when countryGIR is longer than 2 characters" in {
        val invalidCountryGIRJson: JsObject = ornRequestJs.as[JsObject] + ("countryGIR" -> JsString("USA"))
        result(invalidCountryGIRJson) shouldFailWith InvalidJson
      }

      "return InvalidJson response when issuingCountryTIN is longer than 2 characters" in {
        val invalidIssuingCountryTINJson: JsObject = ornRequestJs.as[JsObject] + ("issuingCountryTIN" -> JsString("USA"))
        result(invalidIssuingCountryTINJson) shouldFailWith InvalidJson
      }

      "return InvalidJson response when reportingEntityName is empty" in {
        val invalidReportingEntityNameJson: JsObject = ornRequestJs.as[JsObject] + ("reportingEntityName" -> JsString(""))
        result(invalidReportingEntityNameJson) shouldFailWith InvalidJson
      }

      "return InvalidJson response when TIN is empty" in {
        val invalidTinJson: JsObject = ornRequestJs.as[JsObject] + ("TIN" -> JsString(""))
        result(invalidTinJson) shouldFailWith InvalidJson
      }

      "return InvalidJson response when reportingEntityName exceeds 200 characters" in {
        val longString = "a" * 201
        val invalidLongReportingEntityJson: JsObject = ornRequestJs.as[JsObject] + ("reportingEntityName" -> JsString(longString))
        result(invalidLongReportingEntityJson) shouldFailWith InvalidJson
      }

      "return InvalidJson response when TIN exceeds 200 characters" in {
        val longTin = "a" * 201
        val invalidLongTinJson: JsObject = ornRequestJs.as[JsObject] + ("TIN" -> JsString(longTin))
        result(invalidLongTinJson) shouldFailWith InvalidJson
      }
    }

  }

}
