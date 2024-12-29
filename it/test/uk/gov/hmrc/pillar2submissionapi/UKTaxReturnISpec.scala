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

package uk.gov.hmrc.pillar2submissionapi

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.OptionValues
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.base.IntegrationSpecBase
import uk.gov.hmrc.pillar2submissionapi.controllers.error.{AuthenticationError, Pillar2ErrorResponse}
import uk.gov.hmrc.pillar2submissionapi.controllers.routes
import uk.gov.hmrc.pillar2submissionapi.helpers.UKTRErrorCodes.INVALID_RETURN_093
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.responses.{UKTRSubmitErrorResponse, UKTRSubmitSuccessResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClientV2Provider

import java.net.URI
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

class UKTaxReturnISpec extends IntegrationSpecBase with OptionValues {

  lazy val provider: HttpClientV2Provider = app.injector.instanceOf[HttpClientV2Provider]
  lazy val client:   HttpClientV2         = provider.get()
  lazy val str = s"http://localhost:$port${routes.UKTaxReturnController.submitUktr.url}"
  lazy val baseRequest: RequestBuilder = client.post(URI.create(str).toURL)

  val request:       RequestBuilder       = baseRequest.withBody(validLiabilityReturn)
  def result:        HttpResponse         = Await.result(request.execute[HttpResponse], 5.seconds)
  def errorResponse: Pillar2ErrorResponse = result.json.as[Pillar2ErrorResponse]

  "Create a new UKTR submission (POST)" should {
    "create submission when given valid submission data" in {
      stubGet(s"$readSubscriptionPath/$plrReference", OK, subscriptionSuccess.toString)
      stubResponse("/report-pillar2-top-up-taxes/submit-uk-tax-return", CREATED, Json.toJson(uktrSubmissionSuccessResponse))

      val result = Await.result(request.execute[UKTRSubmitSuccessResponse], 5.seconds)

      result.chargeReference.value mustEqual pillar2Id
      result.formBundleNumber mustEqual formBundleNumber
    }
  }

  "has valid nil-return submission data" should {
    "return a 201 CREATED response" in {
      stubGet(s"$readSubscriptionPath/$plrReference", OK, subscriptionSuccess.toString)
      stubResponse("/report-pillar2-top-up-taxes/submit-uk-tax-return", CREATED, Json.toJson(uktrSubmissionSuccessResponse))

      val request = baseRequest.withBody(validNilReturn)
      val result  = Await.result(request.execute[UKTRSubmitSuccessResponse], 5.seconds)

      result.chargeReference.value mustEqual pillar2Id
      result.formBundleNumber mustEqual formBundleNumber
    }
  }

  "has an invalid request body" should {
    "return a 400 BAD_REQUEST response" in {
      stubGet(s"$readSubscriptionPath/$plrReference", OK, subscriptionSuccess.toString)

      val request = baseRequest.withBody(invalidBody)
      val result  = Await.result(request.execute[HttpResponse], 5.seconds)

      result.status mustEqual BAD_REQUEST
    }
  }

  "has an empty request body" should {
    "return a 400 BAD_REQUEST response " in {
      stubGet(s"$readSubscriptionPath/$plrReference", OK, subscriptionSuccess.toString)

      val request = baseRequest.withBody(JsObject.empty)
      val result  = Await.result(request.execute[HttpResponse], 5.seconds)

      result.status mustEqual BAD_REQUEST
    }
  }
  "has no request body" should {
    "return a 400 BAD_REQUEST response " in {
      stubGet(s"$readSubscriptionPath/$plrReference", OK, subscriptionSuccess.toString)

      val request = baseRequest
      val result  = Await.result(request.execute[HttpResponse], 5.seconds)

      result.status mustEqual BAD_REQUEST
    }
  }

  "has a valid request body containing duplicates fields and additional fields" should {
    "return a 201 CREATED response" in {
      stubGet(s"$readSubscriptionPath/$plrReference", OK, subscriptionSuccess.toString)
      stubResponse("/report-pillar2-top-up-taxes/submit-uk-tax-return", CREATED, Json.toJson(uktrSubmissionSuccessResponse))

      val request = baseRequest.withBody(liabilityReturnDuplicateFields)
      val result  = Await.result(request.execute[UKTRSubmitSuccessResponse], 5.seconds)
      result.chargeReference.value mustEqual pillar2Id
      result.formBundleNumber mustEqual formBundleNumber
    }
  }

  "Subscription data does not exist" should {
    "return a INTERNAL_SERVER_ERROR resulting in a RuntimeException being thrown" in {
      result.status mustEqual INTERNAL_SERVER_ERROR
      errorResponse.code mustEqual "004"
      errorResponse.message mustEqual "No Pillar2 subscription found for XCCVRUGFJG788"
    }
  }

  "User unable to be identified" should {
    "return a INTERNAL_SERVER_ERROR resulting in a RuntimeException being thrown" in {
      when(
        mockAuthConnector.authorise[RetrievalsType](any[Predicate](), any[Retrieval[RetrievalsType]]())(any[HeaderCarrier](), any[ExecutionContext]())
      )
        .thenReturn(
          Future.failed(AuthenticationError("Invalid credentials"))
        )

      result.status mustEqual UNAUTHORIZED
      errorResponse.code mustEqual "003"
      errorResponse.message mustEqual "Invalid credentials"
    }
  }

  "'Invalid Return' response from ETMP returned" should {
    "return a 422 UNPROCESSABLE_ENTITY response" in {
      stubGet(s"$readSubscriptionPath/$plrReference", OK, subscriptionSuccess.toString)
      stubResponse(
        "/report-pillar2-top-up-taxes/submit-uk-tax-return",
        UNPROCESSABLE_ENTITY,
        Json.toJson(UKTRSubmitErrorResponse(INVALID_RETURN_093, "Invalid Return"))
      )

      result.status mustEqual UNPROCESSABLE_ENTITY
      errorResponse.code mustEqual INVALID_RETURN_093
      errorResponse.message mustEqual "Invalid Return"
    }
  }

  "'Unauthorized' response from ETMP returned" should {
    "return a 500 INTERNAL_SERVER_ERROR response" in {
      stubGet(s"$readSubscriptionPath/$plrReference", OK, subscriptionSuccess.toString)
      stubResponse(
        "/report-pillar2-top-up-taxes/submit-uk-tax-return",
        UNAUTHORIZED,
        Json.toJson(UKTRSubmitErrorResponse("001", "Unauthorized"))
      )

      result.status mustEqual INTERNAL_SERVER_ERROR
      errorResponse.code mustEqual "500"
      errorResponse.message mustEqual "Internal Server Error"
    }
  }

  "'Internal Server Error' response from ETMP returned" should {
    "return a 500 INTERNAL_SERVER_ERROR response" in {
      stubGet(s"$readSubscriptionPath/$plrReference", OK, subscriptionSuccess.toString)
      stubResponse(
        "/report-pillar2-top-up-taxes/submit-uk-tax-return",
        INTERNAL_SERVER_ERROR,
        Json.toJson(UKTRSubmitErrorResponse("999", "INTERNAL_SERVER_ERROR"))
      )

      result.status mustEqual INTERNAL_SERVER_ERROR
      errorResponse.code mustEqual "500"
      errorResponse.message mustEqual "Internal Server Error"
    }
  }
}
