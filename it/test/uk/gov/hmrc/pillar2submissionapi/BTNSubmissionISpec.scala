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
import uk.gov.hmrc.pillar2submissionapi.BTNSubmissionISpec._
import uk.gov.hmrc.pillar2submissionapi.base.IntegrationSpecBase
import uk.gov.hmrc.pillar2submissionapi.controllers.submission.routes
import uk.gov.hmrc.pillar2submissionapi.helpers.TestAuthRetrievals.~
import uk.gov.hmrc.pillar2submissionapi.models.belowthresholdnotification.{SubmitBTNErrorResponse, SubmitBTNSuccessResponse}
import uk.gov.hmrc.pillar2submissionapi.models.subscription.SubscriptionSuccess
import uk.gov.hmrc.pillar2submissionapi.services.UKTRSubmitErrorResponse
import uk.gov.hmrc.play.bootstrap.http.HttpClientV2Provider
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue

import java.net.URI
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

class BTNSubmissionISpec extends IntegrationSpecBase with OptionValues {

  lazy val provider: HttpClientV2Provider = app.injector.instanceOf[HttpClientV2Provider]
  lazy val client:   HttpClientV2         = provider.get()
  lazy val str = s"http://localhost:$port${routes.BTNSubmissionController.submitBTN.url}"
  lazy val baseRequest: RequestBuilder =
    client.post(URI.create(str).toURL).setHeader("X-Pillar2-Id" -> plrReference, "Authorization" -> "bearerToken")

  private val submitUrl = "/report-pillar2-top-up-taxes/below-threshold-notification/submit"

  "BTNSubmissionController" when {
    "submitBTN as a organisation" must {
      "return 201 CREATED when given valid submission data" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )
        stubRequest(
          "POST",
          submitUrl,
          CREATED,
          Json.toJson(SubmitBTNSuccessResponse("2022-01-31T09:26:17Z"))
        )

        val result = Await.result(baseRequest.withBody(validRequestJson).execute[SubmitBTNSuccessResponse], 5.seconds)

        result.processingDate mustEqual "2022-01-31T09:26:17Z"
      }

      "return 400 BAD_REQUEST for invalid request body" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val result = Await.result(baseRequest.withBody(invalidRequestJson).execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
      }

      "return 400 BAD_REQUEST for empty request body" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val result = Await.result(baseRequest.withBody(JsObject.empty).execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
      }

      "return 400 BAD_REQUEST for missing request body" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val result = Await.result(baseRequest.execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
      }

      "return 201 CREATED for request with duplicate fields and additional fields" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )
        stubRequest(
          "POST",
          submitUrl,
          CREATED,
          Json.toJson(SubmitBTNSuccessResponse("2022-01-31T09:26:17Z"))
        )

        val result =
          Await.result(baseRequest.withBody(validRequestJson_duplicateFieldsAndAdditionalFields).execute[SubmitBTNSuccessResponse], 5.seconds)

        result.processingDate mustEqual "2022-01-31T09:26:17Z"
      }

      "return 422 UNPROCESSABLE_ENTITY for invalid return from ETMP" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )
        stubRequest(
          "POST",
          submitUrl,
          UNPROCESSABLE_ENTITY,
          Json.toJson(SubmitBTNErrorResponse("093", "Invalid Return"))
        )

        val result = Await.result(baseRequest.withBody(validRequestJson).execute[HttpResponse], 5.seconds)

        result.status mustEqual UNPROCESSABLE_ENTITY
        val errorResponse = result.json.as[UKTRSubmitErrorResponse]
        errorResponse.code mustEqual "093"
        errorResponse.message mustEqual "Invalid Return"
      }

      "return 500 INTERNAL_SERVER_ERROR for unauthorized response from ETMP" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )
        stubRequest(
          "POST",
          submitUrl,
          UNAUTHORIZED,
          Json.toJson(SubmitBTNErrorResponse("001", "Unauthorized"))
        )

        val result = Await.result(baseRequest.withBody(validRequestJson).execute[HttpResponse], 5.seconds)

        result.status mustEqual INTERNAL_SERVER_ERROR
        val errorResponse = result.json.as[UKTRSubmitErrorResponse]
        errorResponse.code mustEqual "500"
        errorResponse.message mustEqual "Internal Server Error"
      }

      "return 500 INTERNAL_SERVER_ERROR for internal server error from ETMP" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )
        stubRequest(
          "POST",
          submitUrl,
          INTERNAL_SERVER_ERROR,
          Json.toJson(SubmitBTNErrorResponse("999", "internal_server_error"))
        )

        val result = Await.result(baseRequest.withBody(validRequestJson).execute[HttpResponse], 5.seconds)

        result.status mustEqual INTERNAL_SERVER_ERROR
        val errorResponse = result.json.as[UKTRSubmitErrorResponse]
        errorResponse.code mustEqual "500"
        errorResponse.message mustEqual "Internal Server Error"
      }
    }

    "submitBTN as an agent" must {
      "return 201 CREATED when given valid submission data" in {
        when(
          mockAuthConnector.authorise[RetrievalsType](ArgumentMatchers.eq(requiredGatewayPredicate), ArgumentMatchers.eq(requiredRetrievals))(using
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(Some(id) ~ Some(groupId) ~ pillar2Enrolments ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType)))
          )

        when(
          mockAuthConnector.authorise[RetrievalsType](ArgumentMatchers.eq(requiredAgentPredicate), ArgumentMatchers.eq(requiredRetrievals))(using
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(Some(id) ~ Some(groupId) ~ pillar2Enrolments ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType)))
          )
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )
        stubRequest(
          "POST",
          submitUrl,
          CREATED,
          Json.toJson(SubmitBTNSuccessResponse("2022-01-31T09:26:17Z"))
        )

        val result =
          Await.result(baseRequest.withBody(validRequestJson).setHeader("X-Pillar2-Id" -> plrReference).execute[SubmitBTNSuccessResponse], 5.seconds)

        result.processingDate mustEqual "2022-01-31T09:26:17Z"
      }
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
