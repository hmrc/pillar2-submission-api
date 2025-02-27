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
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.base.ControllerBaseSpec
import uk.gov.hmrc.pillar2submissionapi.controllers.error.{InvalidRequest, UnexpectedResponse}
import uk.gov.hmrc.pillar2submissionapi.controllers.obligationsandsubmissions.ObligationsAndSubmissionsController
import uk.gov.hmrc.pillar2submissionapi.helpers.ObligationsAndSubmissionsDataFixture

import java.time.LocalDate
import scala.concurrent.Future

class ObligationsAndSubmissionsControllerSpec extends ControllerBaseSpec with ObligationsAndSubmissionsDataFixture {

  val ObligationsAndSubmissionsController: ObligationsAndSubmissionsController =
    new ObligationsAndSubmissionsController(cc, identifierAction, mockObligationsAndSubmissionsService)

  def request(fromDate: String, toDate: String): Future[Result] = ObligationsAndSubmissionsController.retrieveData(fromDate, toDate)(FakeRequest())

  "retrieveData" should {
    "return OK with obligations data when valid dates are provided and service call is successful" in {
      when(mockObligationsAndSubmissionsService.handleData(any[LocalDate], any[LocalDate])(any[HeaderCarrier]))
        .thenReturn(Future.successful(obligationsAndSubmissionsSuccessResponse))

      val result = request(fromDate, toDate)

      status(result) mustEqual OK
    }
  }

  "return InvalidRequest when date format is invalid" in {
    val result = request("invalid-date", toDate)

    intercept[InvalidRequest.type](await(result))
  }

  "return InvalidRequest when date range is invalid" in {
    val result = request(toDate, fromDate)

    intercept[InvalidRequest.type](await(result))
  }

  "return InternalServerError when service call fails" in {
    when(mockObligationsAndSubmissionsService.handleData(any[LocalDate], any[LocalDate])(any[HeaderCarrier]))
      .thenReturn(Future.failed(UnexpectedResponse))

    val result = request(fromDate, toDate)

    intercept[UnexpectedResponse.type](await(result))
  }
}
