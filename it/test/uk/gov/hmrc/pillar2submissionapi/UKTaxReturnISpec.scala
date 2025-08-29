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

import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, postRequestedFor, putRequestedFor, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.OptionValues
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.User
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.base.IntegrationSpecBase
import uk.gov.hmrc.pillar2submissionapi.controllers.submission.routes
import uk.gov.hmrc.pillar2submissionapi.helpers.TestAuthRetrievals.Ops
import uk.gov.hmrc.pillar2submissionapi.helpers.UKTRErrorCodes.INVALID_RETURN_093
import uk.gov.hmrc.pillar2submissionapi.models.response.Pillar2ErrorResponse
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.responses.UKTRSubmitSuccessResponse
import uk.gov.hmrc.pillar2submissionapi.services.UKTRSubmitErrorResponse
import uk.gov.hmrc.play.bootstrap.http.HttpClientV2Provider

import java.net.URI
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

class UKTaxReturnISpec extends IntegrationSpecBase with OptionValues {

  lazy val provider: HttpClientV2Provider = app.injector.instanceOf[HttpClientV2Provider]
  lazy val client:   HttpClientV2         = provider.get()
  lazy val str:      String               = s"http://localhost:$port${routes.UKTaxReturnController.submitUKTR.url}"
  def requestWithBody(body: JsValue = validLiabilityReturn): RequestBuilder =
    client.post(URI.create(str).toURL).setHeader("X-Pillar2-Id" -> plrReference, "Authorization" -> "bearerToken").withBody(body)
  def requestWithBodyAsAgent(body: JsValue = validLiabilityReturn): RequestBuilder =
    client.post(URI.create(str).toURL).setHeader("X-Pillar2-Id" -> plrReference, "Authorization" -> "bearerToken").withBody(body)
  def getSubscriptionStub: StubMapping = stubGet(s"$readSubscriptionPath/$plrReference", OK, subscriptionSuccess.toString)

  private val submitUrl = "/report-pillar2-top-up-taxes/submit-uk-tax-return"
  private val amendUrl  = "/report-pillar2-top-up-taxes/amend-uk-tax-return"

  "UKTaxReturnController" when {
    "submitUKTR by organisation" must {
      "forward the X-Pillar2-Id header" in {
        getSubscriptionStub
        stubRequest(
          "POST",
          submitUrl,
          CREATED,
          Json.toJson(uktrSubmissionSuccessResponse),
          Map("X-Pillar2-Id" -> plrReference)
        )

        val result = Await.result(requestWithBody().execute[UKTRSubmitSuccessResponse], 5.seconds)

        result.chargeReference.value mustEqual pillar2Id
        result.formBundleNumber mustEqual formBundleNumber
        server.verify(
          postRequestedFor(urlEqualTo(submitUrl)).withHeader("X-Pillar2-Id", equalTo(plrReference))
        )
      }

      "return 201 CREATED for valid submission data" in {
        getSubscriptionStub
        stubRequest(
          "POST",
          submitUrl,
          CREATED,
          Json.toJson(uktrSubmissionSuccessResponse)
        )

        val result = Await.result(requestWithBody().execute[UKTRSubmitSuccessResponse], 5.seconds)

        result.chargeReference.value mustEqual pillar2Id
        result.formBundleNumber mustEqual formBundleNumber
      }

      "return 201 CREATED for valid nil-return submission" in {
        getSubscriptionStub
        stubRequest(
          "POST",
          submitUrl,
          CREATED,
          Json.toJson(uktrSubmissionSuccessResponse)
        )

        val result = Await.result(requestWithBody(validNilReturn).execute[UKTRSubmitSuccessResponse], 5.seconds)

        result.chargeReference.value mustEqual pillar2Id
        result.formBundleNumber mustEqual formBundleNumber
      }

      "return 400 BAD_REQUEST for invalid request body" in {
        getSubscriptionStub

        val result = Await.result(requestWithBody(invalidBody).execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
      }

      "return 400 BAD_REQUEST for empty request body" in {
        getSubscriptionStub

        val result = Await.result(requestWithBody(JsObject.empty).execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
      }

      "return 400 BAD_REQUEST when liabilities.liableEntities is empty" in {
        getSubscriptionStub

        val liabilitiesWithEmptyEntities: JsObject =
          (validLiabilityReturn \ "liabilities").as[JsObject] + ("liableEntities" -> Json.arr())
        val bodyWithEmptyEntities: JsValue = validLiabilityReturn.as[JsObject] + ("liabilities" -> liabilitiesWithEmptyEntities)

        val result = Await.result(requestWithBody(bodyWithEmptyEntities).execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
      }

      "return 201 CREATED for request with duplicate fields" in {
        getSubscriptionStub
        stubRequest(
          "POST",
          submitUrl,
          CREATED,
          Json.toJson(uktrSubmissionSuccessResponse)
        )

        val result = Await.result(requestWithBody(liabilityReturnDuplicateFields).execute[UKTRSubmitSuccessResponse], 5.seconds)

        result.chargeReference.value mustEqual pillar2Id
        result.formBundleNumber mustEqual formBundleNumber
      }

      "return 500 INTERNAL_SERVER_ERROR when subscription data does not exist" in {
        val result = Await.result(requestWithBody().execute[HttpResponse], 5.seconds)

        result.status mustEqual INTERNAL_SERVER_ERROR
        val errorResponse = result.json.as[UKTRSubmitErrorResponse]
        errorResponse.code mustEqual "004"
        errorResponse.message mustEqual "No Pillar2 subscription found for XCCVRUGFJG788"
      }

      "return 422 UNPROCESSABLE_ENTITY for invalid return from ETMP" in {
        getSubscriptionStub
        stubRequest(
          "POST",
          submitUrl,
          UNPROCESSABLE_ENTITY,
          Json.toJson(UKTRSubmitErrorResponse(INVALID_RETURN_093, "Invalid Return"))
        )

        val result = Await.result(requestWithBody().execute[HttpResponse], 5.seconds)

        result.status mustEqual UNPROCESSABLE_ENTITY
        val errorResponse = result.json.as[UKTRSubmitErrorResponse]
        errorResponse.code mustEqual INVALID_RETURN_093
        errorResponse.message mustEqual "Invalid Return"
      }

      "return 500 INTERNAL_SERVER_ERROR for unauthorized response from ETMP" in {
        getSubscriptionStub
        stubRequest(
          "POST",
          submitUrl,
          UNAUTHORIZED,
          Json.toJson(UKTRSubmitErrorResponse("001", "Unauthorized"))
        )

        val result = Await.result(requestWithBody().execute[HttpResponse], 5.seconds)

        result.status mustEqual INTERNAL_SERVER_ERROR
        val errorResponse = result.json.as[UKTRSubmitErrorResponse]
        errorResponse.code mustEqual "500"
        errorResponse.message mustEqual "Internal Server Error"
      }

      "return 500 INTERNAL_SERVER_ERROR for internal server error from ETMP" in {
        getSubscriptionStub
        stubRequest(
          "POST",
          submitUrl,
          INTERNAL_SERVER_ERROR,
          Json.toJson(UKTRSubmitErrorResponse("999", "internal_server_error"))
        )

        val result = Await.result(requestWithBody().execute[HttpResponse], 5.seconds)

        result.status mustEqual INTERNAL_SERVER_ERROR
        val errorResponse = result.json.as[UKTRSubmitErrorResponse]
        errorResponse.code mustEqual "500"
        errorResponse.message mustEqual "Internal Server Error"
      }
    }

    "submitUKTR by agent" must {

      "return 201 CREATED for valid submission data" in {
        when(
          mockAuthConnector.authorise[RetrievalsType](ArgumentMatchers.eq(requiredGatewayPredicate), ArgumentMatchers.eq(requiredRetrievals))(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(Some(id) ~ Some(groupId) ~ pillar2Enrolments ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType)))
          )

        when(
          mockAuthConnector.authorise[RetrievalsType](ArgumentMatchers.eq(requiredAgentPredicate), ArgumentMatchers.eq(requiredRetrievals))(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(Some(id) ~ Some(groupId) ~ pillar2Enrolments ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType)))
          )

        getSubscriptionStub
        stubRequest(
          "POST",
          submitUrl,
          CREATED,
          Json.toJson(uktrSubmissionSuccessResponse)
        )

        val result = Await.result(
          requestWithBody().setHeader("X-Pillar2-Id" -> plrReference).execute[HttpResponse],
          5.seconds
        )
        val resultAsResponseModel = result.json.as[UKTRSubmitSuccessResponse]

        result.status mustBe CREATED
        resultAsResponseModel.chargeReference.value mustEqual pillar2Id
        resultAsResponseModel.formBundleNumber mustEqual formBundleNumber
      }
    }

    "amendUKTR by organisation" must {
      val amendRequest: JsValue => RequestBuilder =
        (body: JsValue) =>
          client.put(URI.create(str).toURL).setHeader("X-Pillar2-Id" -> plrReference, "Authorization" -> "bearerToken").withBody(body)

      "forward the X-Pillar2-Id header" in {
        getSubscriptionStub
        stubRequest(
          "PUT",
          amendUrl,
          OK,
          Json.toJson(uktrSubmissionSuccessResponse),
          Map("X-Pillar2-Id" -> plrReference)
        )

        val result = Await.result(amendRequest(validLiabilityReturn).execute[UKTRSubmitSuccessResponse], 5.seconds)

        result.chargeReference.value mustEqual pillar2Id
        result.formBundleNumber mustEqual formBundleNumber
        server.verify(
          putRequestedFor(urlEqualTo(amendUrl)).withHeader("X-Pillar2-Id", equalTo(plrReference))
        )
      }

      "return 200 OK for valid submission data" in {
        getSubscriptionStub
        stubRequest(
          "PUT",
          amendUrl,
          OK,
          Json.toJson(uktrSubmissionSuccessResponse)
        )

        val result = Await.result(amendRequest(validLiabilityReturn).execute[UKTRSubmitSuccessResponse], 5.seconds)

        result.chargeReference.value mustEqual pillar2Id
        result.formBundleNumber mustEqual formBundleNumber
      }

      "return 200 OK for valid nil-return submission" in {
        getSubscriptionStub
        stubRequest(
          "PUT",
          amendUrl,
          OK,
          Json.toJson(uktrSubmissionSuccessResponse)
        )

        val result = Await.result(amendRequest(validNilReturn).execute[UKTRSubmitSuccessResponse], 5.seconds)

        result.chargeReference.value mustEqual pillar2Id
        result.formBundleNumber mustEqual formBundleNumber
      }

      "return 400 BAD_REQUEST for invalid request body" in {
        getSubscriptionStub

        val result = Await.result(amendRequest(invalidBody).execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
      }

      "return 400 BAD_REQUEST for empty request body" in {
        getSubscriptionStub

        val result = Await.result(amendRequest(JsObject.empty).execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
      }

      "return 200 OK for request with duplicate fields" in {
        getSubscriptionStub
        stubRequest(
          "PUT",
          amendUrl,
          OK,
          Json.toJson(uktrSubmissionSuccessResponse)
        )

        val result = Await.result(amendRequest(liabilityReturnDuplicateFields).execute[UKTRSubmitSuccessResponse], 5.seconds)

        result.chargeReference.value mustEqual pillar2Id
        result.formBundleNumber mustEqual formBundleNumber
      }

      "return 400 BAD_REQUEST when liabilities.liableEntities is empty" in {
        getSubscriptionStub

        val liabilitiesWithEmptyEntities: JsObject =
          (validLiabilityReturn \ "liabilities").as[JsObject] + ("liableEntities" -> Json.arr())
        val bodyWithEmptyEntities: JsValue = validLiabilityReturn.as[JsObject] + ("liabilities" -> liabilitiesWithEmptyEntities)

        val result = Await.result(amendRequest(bodyWithEmptyEntities).execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
      }

      "return 500 INTERNAL_SERVER_ERROR when subscription data does not exist" in {
        val result = Await.result(amendRequest(validLiabilityReturn).execute[HttpResponse], 5.seconds)

        result.status mustEqual INTERNAL_SERVER_ERROR
        val errorResponse = result.json.as[Pillar2ErrorResponse]
        errorResponse.code mustEqual "004"
        errorResponse.message mustEqual "No Pillar2 subscription found for XCCVRUGFJG788"
      }

      "return 422 UNPROCESSABLE_ENTITY for invalid return from ETMP" in {
        getSubscriptionStub
        stubRequest(
          "PUT",
          amendUrl,
          UNPROCESSABLE_ENTITY,
          Json.toJson(UKTRSubmitErrorResponse(INVALID_RETURN_093, "Invalid Return"))
        )

        val result = Await.result(amendRequest(validLiabilityReturn).execute[HttpResponse], 5.seconds)

        result.status mustEqual UNPROCESSABLE_ENTITY
        val errorResponse = result.json.as[Pillar2ErrorResponse]
        errorResponse.code mustEqual INVALID_RETURN_093
        errorResponse.message mustEqual "Invalid Return"
      }

      "return 500 INTERNAL_SERVER_ERROR for unauthorized response from ETMP" in {
        getSubscriptionStub
        stubRequest(
          "PUT",
          amendUrl,
          UNAUTHORIZED,
          Json.toJson(UKTRSubmitErrorResponse("001", "Unauthorized"))
        )

        val result = Await.result(amendRequest(validLiabilityReturn).execute[HttpResponse], 5.seconds)

        result.status mustEqual INTERNAL_SERVER_ERROR
        val errorResponse = result.json.as[Pillar2ErrorResponse]
        errorResponse.code mustEqual "500"
        errorResponse.message mustEqual "Internal Server Error"
      }

      "return 500 INTERNAL_SERVER_ERROR for internal server error from ETMP" in {
        getSubscriptionStub
        stubRequest(
          "PUT",
          amendUrl,
          INTERNAL_SERVER_ERROR,
          Json.toJson(UKTRSubmitErrorResponse("999", "internal_server_error"))
        )

        val result = Await.result(amendRequest(validLiabilityReturn).execute[HttpResponse], 5.seconds)

        result.status mustEqual INTERNAL_SERVER_ERROR
        val errorResponse = result.json.as[Pillar2ErrorResponse]
        errorResponse.code mustEqual "500"
        errorResponse.message mustEqual "Internal Server Error"
      }
    }

    "amendUKTR by agent" must {

      "return 200 OK for valid submission data" in {
        when(
          mockAuthConnector.authorise[RetrievalsType](ArgumentMatchers.eq(requiredGatewayPredicate), ArgumentMatchers.eq(requiredRetrievals))(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(Some(id) ~ Some(groupId) ~ pillar2Enrolments ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType)))
          )

        when(
          mockAuthConnector.authorise[RetrievalsType](ArgumentMatchers.eq(requiredAgentPredicate), ArgumentMatchers.eq(requiredRetrievals))(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(Some(id) ~ Some(groupId) ~ pillar2Enrolments ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType)))
          )

        getSubscriptionStub
        stubRequest(
          "PUT",
          amendUrl,
          OK,
          Json.toJson(uktrSubmissionSuccessResponse)
        )

        val amendRequest: JsValue => RequestBuilder =
          (body: JsValue) =>
            client.put(URI.create(str).toURL).withBody(body).setHeader("X-Pillar2-Id" -> plrReference, "Authorization" -> "bearerToken")
        val result = Await.result(amendRequest(validLiabilityReturn).execute[UKTRSubmitSuccessResponse], 5.seconds)

        result.chargeReference.value mustEqual pillar2Id
        result.formBundleNumber mustEqual formBundleNumber
      }
    }
  }
}
