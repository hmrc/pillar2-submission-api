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

package uk.gov.hmrc.pillar2submissionapi.controllers.test

import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.base.ControllerBaseSpec
import uk.gov.hmrc.pillar2submissionapi.config.AppConfig
import uk.gov.hmrc.pillar2submissionapi.controllers.error.{EmptyRequestBody, InvalidJson, TestEndpointDisabled}
import uk.gov.hmrc.pillar2submissionapi.models.globeinformationreturn.{GIRSubmission, GIRSuccess, SubmitGIRSuccessResponse}
import uk.gov.hmrc.pillar2submissionapi.services.GIRService

import java.time.LocalDate
import scala.concurrent.Future

class GIRControllerSpec extends ControllerBaseSpec {

  val mockAppConfig:  AppConfig  = mock[AppConfig]
  val mockGIRService: GIRService = mock[GIRService]

  def controller(testEndpointsEnabled: Boolean = true): GIRController = {
    when(mockAppConfig.testOrganisationEnabled).thenReturn(testEndpointsEnabled)
    new GIRController(cc, identifierAction, pillar2IdAction, mockGIRService, mockAppConfig)
  }

  val validSubmission:  GIRSubmission = GIRSubmission(LocalDate.parse("2024-01-01"), LocalDate.parse("2024-12-31"))
  val validRequestJson: JsValue       = Json.obj(
    "accountingPeriodFrom" -> "2024-01-01",
    "accountingPeriodTo"   -> "2024-12-31"
  )
  val validResponse:     SubmitGIRSuccessResponse = SubmitGIRSuccessResponse(GIRSuccess("2024-01-01T12:00:00Z"))
  val validResponseJson: JsValue                  = Json.toJson(validResponse)

  "GIRController" when {
    "test endpoints are enabled" when {
      "createGIR" must {
        "return 201 CREATED for valid request" in {
          when(mockGIRService.createGIR(eqTo(validSubmission))(using any[HeaderCarrier]))
            .thenReturn(Future.successful(validResponse))

          val result = controller().createGIR(FakeRequest().withHeaders("X-Pillar2-Id" -> pillar2Id).withJsonBody(validRequestJson))

          status(result) mustBe CREATED
          contentAsJson(result) mustBe validResponseJson
        }
        "return InvalidJson for invalid request" in {
          val invalidJson = Json.obj("badField" -> "badValue")
          val result      = controller().createGIR(FakeRequest().withHeaders("X-Pillar2-Id" -> pillar2Id).withJsonBody(invalidJson))
          result.shouldFailWith(InvalidJson)
        }
        "return EmptyRequestBody for missing body" in {
          val result = controller().createGIR(FakeRequest().withHeaders("X-Pillar2-Id" -> pillar2Id))
          result.shouldFailWith(EmptyRequestBody)
        }
      }
    }
    "test endpoints are disabled" when {
      "createGIR" must {
        "return 403 FORBIDDEN" in {
          val result = controller(testEndpointsEnabled = false).createGIR(
            FakeRequest().withHeaders("X-Pillar2-Id" -> pillar2Id).withJsonBody(validRequestJson)
          )
          result.shouldFailWith(TestEndpointDisabled)
        }
      }
    }
  }
}
