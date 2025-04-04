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
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Retrieval}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.ORNSubmissionISpec._
import uk.gov.hmrc.pillar2submissionapi.base.IntegrationSpecBase
import uk.gov.hmrc.pillar2submissionapi.controllers.error.AuthenticationError
import uk.gov.hmrc.pillar2submissionapi.controllers.submission.routes
import uk.gov.hmrc.pillar2submissionapi.helpers.TestAuthRetrievals.Ops
import uk.gov.hmrc.pillar2submissionapi.models.overseasreturnnotification.{ORNErrorResponse, ORNSuccessResponse}
import uk.gov.hmrc.pillar2submissionapi.models.subscription.SubscriptionSuccess
import uk.gov.hmrc.play.bootstrap.http.HttpClientV2Provider

import java.net.URI
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

class ORNSubmissionISpec extends IntegrationSpecBase with OptionValues {

  lazy val provider: HttpClientV2Provider = app.injector.instanceOf[HttpClientV2Provider]
  lazy val client:   HttpClientV2         = provider.get()
  lazy val str = s"http://localhost:$port${routes.ORNSubmissionController.submitORN.url}"
  lazy val baseRequest: RequestBuilder = client.post(URI.create(str).toURL).setHeader("X-Pillar2-Id" -> plrReference)

  private val submitUrl = "/report-pillar2-top-up-taxes/overseas-return-notification/submit"

  "ORNSubmissionController" when {
    "submitORN as a organisation" must {
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
          Json.toJson(ORNSuccessResponse("2022-01-31T09:26:17Z", "123456789012345"))
        )

        val result = Await.result(baseRequest.withBody(validRequestJson).execute[ORNSuccessResponse], 5.seconds)

        result.processingDate mustEqual "2022-01-31T09:26:17Z"
        result.formBundleNumber mustEqual "123456789012345"
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
          Json.toJson(ORNSuccessResponse("2022-01-31T09:26:17Z", "123456789012345"))
        )

        val result =
          Await.result(baseRequest.withBody(validRequestJson_duplicateFieldsAndAdditionalFields).execute[ORNSuccessResponse], 5.seconds)

        result.processingDate mustEqual "2022-01-31T09:26:17Z"
        result.formBundleNumber mustEqual "123456789012345"
      }

      "return 401 UNAUTHORIZED when user cannot be identified" in {
        when(
          mockAuthConnector
            .authorise[RetrievalsType](any[Predicate](), any[Retrieval[RetrievalsType]]())(any[HeaderCarrier](), any[ExecutionContext]())
        ).thenReturn(Future.failed(AuthenticationError))

        val result = Await.result(baseRequest.withBody(validRequestJson).execute[HttpResponse], 5.seconds)

        result.status mustEqual UNAUTHORIZED
        val errorResponse = result.json.as[ORNErrorResponse]
        errorResponse.code mustEqual "003"
        errorResponse.message mustEqual "Not authorized"
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
          Json.toJson(ORNErrorResponse("093", "Invalid Return"))
        )

        val result = Await.result(baseRequest.withBody(validRequestJson).execute[HttpResponse], 5.seconds)

        result.status mustEqual UNPROCESSABLE_ENTITY
        val errorResponse = result.json.as[ORNErrorResponse]
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
          Json.toJson(ORNErrorResponse("001", "Unauthorized"))
        )

        val result = Await.result(baseRequest.withBody(validRequestJson).execute[HttpResponse], 5.seconds)

        result.status mustEqual INTERNAL_SERVER_ERROR
        val errorResponse = result.json.as[ORNErrorResponse]
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
          Json.toJson(ORNErrorResponse("999", "internal_server_error"))
        )

        val result = Await.result(baseRequest.withBody(validRequestJson).execute[HttpResponse], 5.seconds)

        result.status mustEqual INTERNAL_SERVER_ERROR
        val errorResponse = result.json.as[ORNErrorResponse]
        errorResponse.code mustEqual "500"
        errorResponse.message mustEqual "Internal Server Error"
      }
    }

    "submitORN as an agent" must {
      "return 201 CREATED when given valid submission data" in {
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
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )
        stubRequest(
          "POST",
          submitUrl,
          CREATED,
          Json.toJson(ORNSuccessResponse("2022-01-31T09:26:17Z", "123456789012345"))
        )

        val result =
          Await.result(baseRequest.withBody(validRequestJson).setHeader("X-Pillar2-Id" -> plrReference).execute[ORNSuccessResponse], 5.seconds)

        result.processingDate mustEqual "2022-01-31T09:26:17Z"
        result.formBundleNumber mustEqual "123456789012345"
      }
    }
  }
}

object ORNSubmissionISpec {
  val validRequestJson: JsValue =
    Json.parse("""{
                 |  "accountingPeriodFrom": "2023-01-01",
                 |  "accountingPeriodTo": "2024-12-31",
                 |  "filedDateGIR": "2024-12-31",
                 |  "countryGIR":"US",
                 |  "reportingEntityName" : "Newco PLC",
                 |  "TIN" : "US12345678",
                 |  "issuingCountryTIN" : "US"
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
                 |  "extraField1": "value2",
                 |  "filedDateGIR": "2024-12-31",
                 |  "countryGIR":"US",
                 |  "reportingEntityName" : "Newco PLC",
                 |  "TIN" : "US12345678",
                 |  "issuingCountryTIN" : "US"
                 |}""".stripMargin)
}
