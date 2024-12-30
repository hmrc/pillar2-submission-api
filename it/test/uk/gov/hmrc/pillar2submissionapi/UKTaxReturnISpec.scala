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

import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, postRequestedFor, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.OptionValues
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
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
  lazy val str:      String               = s"http://localhost:$port${routes.UKTaxReturnController.submitUktr.url}"
  def requestWithBody(body: JsValue = validLiabilityReturn): RequestBuilder = client.post(URI.create(str).toURL).withBody(body)
  def getSubscriptionStub: StubMapping = stubGet(s"$readSubscriptionPath/$plrReference", OK, subscriptionSuccess.toString)

  "Create a new UKTR submission (POST)" should {
    "forward the X-Pillar2-Id header" in {
      getSubscriptionStub
      implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("X-Pillar2-Id" -> plrReference)
      stubResponseWithExtraHeader("/report-pillar2-top-up-taxes/submit-uk-tax-return", CREATED, Json.toJson(uktrSubmissionSuccessResponse))

      val result = Await.result(requestWithBody().execute[UKTRSubmitSuccessResponse], 5.seconds)

      result.chargeReference.value mustEqual pillar2Id
      result.formBundleNumber mustEqual formBundleNumber
      server.verify(
        postRequestedFor(urlEqualTo("/report-pillar2-top-up-taxes/submit-uk-tax-return")).withHeader("X-Pillar2-Id", equalTo(plrReference))
      )
    }

    "create submission when given valid submission data" in {
      getSubscriptionStub
      stubResponse("/report-pillar2-top-up-taxes/submit-uk-tax-return", CREATED, Json.toJson(uktrSubmissionSuccessResponse))

      val result = Await.result(requestWithBody().execute[UKTRSubmitSuccessResponse], 5.seconds)

      result.chargeReference.value mustEqual pillar2Id
      result.formBundleNumber mustEqual formBundleNumber
    }
  }

  "has valid nil-return submission data" should {
    "return a 201 CREATED response" in {
      getSubscriptionStub
      stubResponse("/report-pillar2-top-up-taxes/submit-uk-tax-return", CREATED, Json.toJson(uktrSubmissionSuccessResponse))

      val result = Await.result(requestWithBody(validNilReturn).execute[UKTRSubmitSuccessResponse], 5.seconds)

      result.chargeReference.value mustEqual pillar2Id
      result.formBundleNumber mustEqual formBundleNumber
    }
  }

  "has an invalid request body" should {
    "return a 400 BAD_REQUEST response" in {
      getSubscriptionStub

      val result = Await.result(requestWithBody(invalidBody).execute[HttpResponse], 5.seconds)

      result.status mustEqual BAD_REQUEST
    }
  }

  "has an empty request body" should {
    "return a 400 BAD_REQUEST response " in {
      getSubscriptionStub

      val result = Await.result(requestWithBody(JsObject.empty).execute[HttpResponse], 5.seconds)

      result.status mustEqual BAD_REQUEST
    }
  }

  "has a valid request body containing duplicates fields and additional fields" should {
    "return a 201 CREATED response" in {
      getSubscriptionStub
      stubResponse("/report-pillar2-top-up-taxes/submit-uk-tax-return", CREATED, Json.toJson(uktrSubmissionSuccessResponse))

      val result = Await.result(requestWithBody(liabilityReturnDuplicateFields).execute[UKTRSubmitSuccessResponse], 5.seconds)

      result.chargeReference.value mustEqual pillar2Id
      result.formBundleNumber mustEqual formBundleNumber
    }
  }

  "Subscription data does not exist" should {
    "return a InternalServerError resulting in a RuntimeException being thrown" in {
      val result = Await.result(requestWithBody().execute[HttpResponse], 5.seconds)
      result.status mustEqual INTERNAL_SERVER_ERROR

      val errorResponse = result.json.as[Pillar2ErrorResponse]
      errorResponse.code mustEqual "004"
      errorResponse.message mustEqual "No Pillar2 subscription found for XCCVRUGFJG788"
    }
  }

  "User unable to be identified" should {
    "return a InternalServerError resulting in a RuntimeException being thrown" in {
      when(
        mockAuthConnector.authorise[RetrievalsType](any[Predicate](), any[Retrieval[RetrievalsType]]())(any[HeaderCarrier](), any[ExecutionContext]())
      )
        .thenReturn(Future.failed(AuthenticationError("Invalid credentials")))

      val result = Await.result(requestWithBody().execute[HttpResponse], 5.seconds)

      result.status mustEqual UNAUTHORIZED
      val errorResponse = result.json.as[Pillar2ErrorResponse]
      errorResponse.code mustEqual "003"
      errorResponse.message mustEqual "Invalid credentials"
    }
  }

  "'Invalid Return' response from ETMP returned" should {
    "return a 422 UNPROCESSABLE_ENTITY response" in {
      getSubscriptionStub
      stubResponse(
        "/report-pillar2-top-up-taxes/submit-uk-tax-return",
        UNPROCESSABLE_ENTITY,
        Json.toJson(UKTRSubmitErrorResponse(INVALID_RETURN_093, "Invalid Return"))
      )

      val result = Await.result(requestWithBody().execute[HttpResponse], 5.seconds)

      result.status mustEqual UNPROCESSABLE_ENTITY
      val errorResponse = result.json.as[Pillar2ErrorResponse]
      errorResponse.code mustEqual INVALID_RETURN_093
      errorResponse.message mustEqual "Invalid Return"
    }
  }

  "'Unauthorized' response from ETMP returned" should {
    "return a 500 INTERNAL_SERVER_ERROR response" in {
      getSubscriptionStub
      stubResponse(
        "/report-pillar2-top-up-taxes/submit-uk-tax-return",
        UNAUTHORIZED,
        Json.toJson(UKTRSubmitErrorResponse("001", "Unauthorized"))
      )

      val result = Await.result(requestWithBody().execute[HttpResponse], 5.seconds)

      result.status mustEqual INTERNAL_SERVER_ERROR
      val errorResponse = result.json.as[Pillar2ErrorResponse]
      errorResponse.code mustEqual "500"
      errorResponse.message mustEqual "Internal Server Error"
    }
  }

  "'internal server error' response from ETMP returned" should {
    "return a 500 INTERNAL_SERVER_ERROR response" in {
      getSubscriptionStub
      stubResponse(
        "/report-pillar2-top-up-taxes/submit-uk-tax-return",
        INTERNAL_SERVER_ERROR,
        Json.toJson(UKTRSubmitErrorResponse("999", "internal_server_error"))
      )

      val result = Await.result(requestWithBody().execute[HttpResponse], 5.seconds)

      result.status mustEqual INTERNAL_SERVER_ERROR
      val errorResponse = result.json.as[Pillar2ErrorResponse]
      errorResponse.code mustEqual "500"
      errorResponse.message mustEqual "Internal Server Error"
    }
  }
}
