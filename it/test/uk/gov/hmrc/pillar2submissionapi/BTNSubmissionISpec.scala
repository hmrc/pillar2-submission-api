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
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.base.IntegrationSpecBase
import uk.gov.hmrc.pillar2submissionapi.controllers.error.AuthenticationError
import uk.gov.hmrc.pillar2submissionapi.controllers.routes
import uk.gov.hmrc.pillar2submissionapi.models.subscription.SubscriptionSuccess
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.responses.{SubmitBTNErrorResponse, SubmitBTNSuccessResponse, UKTRSubmitErrorResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClientV2Provider
import BTNSubmissionISpec._
import java.net.URI
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

class BTNSubmissionISpec extends IntegrationSpecBase with OptionValues {

  lazy val provider: HttpClientV2Provider = app.injector.instanceOf[HttpClientV2Provider]
  lazy val client:   HttpClientV2         = provider.get()
  lazy val str = s"http://localhost:$port${routes.BTNSubmissionController.submitBTN.url}"
  lazy val baseRequest: RequestBuilder = client.post(URI.create(str).toURL)

  "Create a new BTN submission (POST)" should {
    "create submission when given valid submission data" in {
      stubGet(
        "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
        OK,
        Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
      )
      stubResponse(
        "/report-pillar2-top-up-taxes/below-threshold-notification/submit",
        CREATED,
        Json.toJson(SubmitBTNSuccessResponse("2022-01-31T09:26:17Z", "119000004320", Some("XTC01234123412")))
      )
      val request = baseRequest.withBody(validRequestJson)
      val result  = Await.result(request.execute[SubmitBTNSuccessResponse], 5.seconds)
      result.chargeReference.value mustEqual "XTC01234123412"
      result.formBundleNumber mustEqual "119000004320"
    }
  }

  "has an invalid request body" should {
    "return a 400 BAD_REQUEST response" in {
      stubGet(
        "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
        OK,
        Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
      )
      val request = baseRequest.withBody(invalidRequestJson)
      val result  = Await.result(request.execute[HttpResponse], 5.seconds)
      println(result.body)
      result.status mustEqual 400
    }
  }

  "has an empty request body" should {
    "return a 400 BAD_REQUEST response " in {
      stubGet(
        "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
        OK,
        Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
      )
      val request = baseRequest.withBody(JsObject.empty)
      val result  = Await.result(request.execute[HttpResponse], 5.seconds)
      result.status mustEqual 400
    }
  }

  "has no request body" should {
    "return a 400 BAD_REQUEST response " in {
      stubGet(
        "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
        OK,
        Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
      )
      val request = baseRequest
      val result  = Await.result(request.execute[HttpResponse], 5.seconds)
      result.status mustEqual 400
    }
  }

  "has a valid request body containing duplicates fields and additional fields" should {
    "return a 201 CREATED response" in {
      stubGet(
        "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
        OK,
        Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
      )
      stubResponse(
        "/report-pillar2-top-up-taxes/below-threshold-notification/submit",
        CREATED,
        Json.toJson(SubmitBTNSuccessResponse("2022-01-31T09:26:17Z", "119000004320", Some("XTC01234123412")))
      )
      val request = baseRequest.withBody(validRequestJson_duplicateFieldsAndAdditionalFields)
      val result  = Await.result(request.execute[SubmitBTNSuccessResponse], 5.seconds)
      result.chargeReference.value mustEqual "XTC01234123412"
      result.formBundleNumber mustEqual "119000004320"
    }
  }

  "User unable to be identified" should {
    "return a InternalServerError resulting in a RuntimeException being thrown" in {
      when(
        mockAuthConnector.authorise[RetrievalsType](any[Predicate](), any[Retrieval[RetrievalsType]]())(any[HeaderCarrier](), any[ExecutionContext]())
      )
        .thenReturn(
          Future.failed(AuthenticationError("Invalid credentials"))
        )
      val request = baseRequest.withBody(validRequestJson)

      val result = Await.result(request.execute[HttpResponse], 5.seconds)
      result.status mustEqual 401
      val errorResponse = result.json.as[UKTRSubmitErrorResponse]
      errorResponse.code mustEqual "003"
      errorResponse.message mustEqual "Invalid credentials"
    }
  }

  "'Invalid Return' response from ETMP returned" should {
    "return a 422 UNPROCESSABLE_ENTITY response" in {
      stubGet(
        "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
        OK,
        Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
      )
      stubResponse(
        "/report-pillar2-top-up-taxes/below-threshold-notification/submit",
        UNPROCESSABLE_ENTITY,
        Json.toJson(SubmitBTNErrorResponse("093", "Invalid Return"))
      )
      val request = baseRequest.withBody(validRequestJson)
      val result  = Await.result(request.execute[HttpResponse], 5.seconds)
      result.status mustEqual 422
      val errorResponse = result.json.as[UKTRSubmitErrorResponse]
      errorResponse.code mustEqual "093"
      errorResponse.message mustEqual "Invalid Return"
    }
  }

  "'Unauthorized' response from ETMP returned" should {
    "return a 500 INTERNAL_SERVER_ERROR response" in {
      stubGet(
        "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
        OK,
        Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
      )
      stubResponse(
        "/report-pillar2-top-up-taxes/below-threshold-notification/submit",
        UNAUTHORIZED,
        Json.toJson(SubmitBTNErrorResponse("001", "Unauthorized"))
      )
      val request = baseRequest.withBody(validRequestJson)
      val result  = Await.result(request.execute[HttpResponse], 5.seconds)
      result.status mustEqual 500
      val errorResponse = result.json.as[UKTRSubmitErrorResponse]
      errorResponse.code mustEqual "500"
      errorResponse.message mustEqual "Internal Server Error"
    }
  }

  "'internal server error' response from ETMP returned" should {
    "return a 500 INTERNAL_SERVER_ERROR response" in {
      stubGet(
        "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
        OK,
        Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
      )
      stubResponse(
        "/report-pillar2-top-up-taxes/below-threshold-notification/submit",
        INTERNAL_SERVER_ERROR,
        Json.toJson(SubmitBTNErrorResponse("999", "internal_server_error"))
      )
      val request = baseRequest.withBody(validRequestJson)
      val result  = Await.result(request.execute[HttpResponse], 5.seconds)
      result.status mustEqual 500
      val errorResponse = result.json.as[UKTRSubmitErrorResponse]
      errorResponse.code mustEqual "500"
      errorResponse.message mustEqual "Internal Server Error"
    }
  }
}

object BTNSubmissionISpec {

  val validRequestJson: JsValue =
    Json.parse("""{
                 |  "accountingPeriodFrom": "2023-01-01",
                 |  "accountingPeriodTo": "2024-12-31"
                 |}""".stripMargin)

  val invalidRequestJson: JsValue =
    Json.parse("""{
                 |  "badRequest": ""
                 |}""".stripMargin)

  val validRequestJson_duplicateFieldsAndAdditionalFields: JsValue =
    Json.parse("""{
                 |  "accountingPeriodFrom": "2023-01-01",
                 |  "accountingPeriodTo": "2024-12-31",
                 |  "accountingPeriodFrom": "2023-01-01",
                 |  "accountingPeriodTo": "2024-12-31",
                 |  "extraField1": "value1",
                 |  "extraField1": "value2"
                 |}""".stripMargin)

}
