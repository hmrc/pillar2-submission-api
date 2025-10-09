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
import uk.gov.hmrc.pillar2submissionapi.OverseasReturnNotificationISpec._
import uk.gov.hmrc.pillar2submissionapi.base.IntegrationSpecBase
import uk.gov.hmrc.pillar2submissionapi.controllers.submission.routes
import uk.gov.hmrc.pillar2submissionapi.helpers.ORNDataFixture
import uk.gov.hmrc.pillar2submissionapi.helpers.TestAuthRetrievals.Ops
import uk.gov.hmrc.pillar2submissionapi.models.overseasreturnnotification.{ORNErrorResponse, ORNRetrieveSuccessResponse, ORNSuccessResponse}
import uk.gov.hmrc.pillar2submissionapi.models.subscription.SubscriptionSuccess
import uk.gov.hmrc.play.bootstrap.http.HttpClientV2Provider
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue

import java.net.URI
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

class OverseasReturnNotificationISpec extends IntegrationSpecBase with OptionValues with ORNDataFixture {

  lazy val provider: HttpClientV2Provider = app.injector.instanceOf[HttpClientV2Provider]
  lazy val client:   HttpClientV2         = provider.get()
  lazy val submitStr = s"http://localhost:$port${routes.OverseasReturnNotificationController.submitORN.url}"
  lazy val amendStr  = s"http://localhost:$port${routes.OverseasReturnNotificationController.amendORN.url}"
  lazy val submitRequest: RequestBuilder =
    client.post(URI.create(submitStr).toURL).setHeader("X-Pillar2-Id" -> plrReference, "Authorization" -> "bearerToken")
  lazy val amendRequest: RequestBuilder =
    client.put(URI.create(amendStr).toURL).setHeader("X-Pillar2-Id" -> plrReference, "Authorization" -> "bearerToken")

  private val submitUrl                             = "/report-pillar2-top-up-taxes/overseas-return-notification/submit"
  private val amendUrl                              = "/report-pillar2-top-up-taxes/overseas-return-notification/amend"
  private def retrieveUrl(from: String, to: String) =
    s"/report-pillar2-top-up-taxes/overseas-return-notification/$from/$to"

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

        val result = Await.result(submitRequest.withBody(validRequestJson).execute[ORNSuccessResponse], 5.seconds)

        result.processingDate mustEqual "2022-01-31T09:26:17Z"
        result.formBundleNumber mustEqual "123456789012345"
      }

      "return 400 BAD_REQUEST for invalid request body" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val result = Await.result(submitRequest.withBody(invalidRequestJson).execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
      }

      "return 400 BAD_REQUEST for empty request body" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val result = Await.result(submitRequest.withBody(JsObject.empty).execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
      }

      "return 400 BAD_REQUEST for missing request body" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val result = Await.result(submitRequest.execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
      }

      "return 400 BAD_REQUEST when countryGIR is longer than 2 characters" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val result = Await.result(submitRequest.withBody(invalidCountryGIRJson).execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
        val errorResponse = result.json.as[ORNErrorResponse]
        errorResponse.code mustEqual "INVALID_JSON"
        errorResponse.message mustEqual "Invalid JSON payload"
      }

      "return 400 BAD_REQUEST when issuingCountryTIN is longer than 2 characters" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val result = Await.result(submitRequest.withBody(invalidIssuingCountryTINJson).execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
        result.json.as[ORNErrorResponse].code mustEqual "INVALID_JSON"
        result.json.as[ORNErrorResponse].message mustEqual "Invalid JSON payload"
      }

      "return 400 BAD_REQUEST when reportingEntityName is empty" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val result = Await.result(submitRequest.withBody(emptyReportingEntityNameJson).execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
        result.json.as[ORNErrorResponse].code mustEqual "INVALID_JSON"
        result.json.as[ORNErrorResponse].message mustEqual "Invalid JSON payload"
      }

      "return 400 BAD_REQUEST when TIN is empty" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val result = Await.result(submitRequest.withBody(emptyTINJson).execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
        result.json.as[ORNErrorResponse].code mustEqual "INVALID_JSON"
        result.json.as[ORNErrorResponse].message mustEqual "Invalid JSON payload"
      }

      "return 400 BAD_REQUEST when reportingEntityName exceeds 200 characters" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val result = Await.result(submitRequest.withBody(longReportingEntityNameJson).execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
        result.json.as[ORNErrorResponse].code mustEqual "INVALID_JSON"
        result.json.as[ORNErrorResponse].message mustEqual "Invalid JSON payload"
      }

      "return 400 BAD_REQUEST when TIN exceeds 200 characters" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val result = Await.result(submitRequest.withBody(longTINJson).execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
        result.json.as[ORNErrorResponse].code mustEqual "INVALID_JSON"
        result.json.as[ORNErrorResponse].message mustEqual "Invalid JSON payload"
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
          Await.result(submitRequest.withBody(validRequestJson_duplicateFieldsAndAdditionalFields).execute[ORNSuccessResponse], 5.seconds)

        result.processingDate mustEqual "2022-01-31T09:26:17Z"
        result.formBundleNumber mustEqual "123456789012345"
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

        val result = Await.result(submitRequest.withBody(validRequestJson).execute[HttpResponse], 5.seconds)

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
          Json.toJson(ORNErrorResponse("500", "Internal Server Error"))
        )

        val result = Await.result(submitRequest.withBody(validRequestJson).execute[HttpResponse], 5.seconds)

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
          Json.toJson(ORNErrorResponse("500", "Internal Server Error"))
        )

        val result = Await.result(submitRequest.withBody(validRequestJson).execute[HttpResponse], 5.seconds)

        result.status mustEqual INTERNAL_SERVER_ERROR
        val errorResponse = result.json.as[ORNErrorResponse]
        errorResponse.code mustEqual "500"
        errorResponse.message mustEqual "Internal Server Error"
      }
    }
    "amendORN as a organisation" must {
      "return 200 CREATED when given valid submission data" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )
        stubRequest(
          "PUT",
          amendUrl,
          OK,
          Json.toJson(ORNSuccessResponse("2022-01-31T09:26:17Z", "123456789012345"))
        )

        val result = Await.result(amendRequest.withBody(validRequestJson).execute[ORNSuccessResponse], 5.seconds)

        result.processingDate mustEqual "2022-01-31T09:26:17Z"
        result.formBundleNumber mustEqual "123456789012345"
      }

      "return 400 BAD_REQUEST for invalid request body" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val result = Await.result(amendRequest.withBody(invalidRequestJson).execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
      }

      "return 400 BAD_REQUEST for empty request body" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val result = Await.result(amendRequest.withBody(JsObject.empty).execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
      }

      "return 400 BAD_REQUEST for missing request body" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val result = Await.result(amendRequest.execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
      }

      "return 400 BAD_REQUEST when countryGIR is longer than 2 characters" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val result = Await.result(amendRequest.withBody(invalidCountryGIRJson).execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
        val errorResponse = result.json.as[ORNErrorResponse]
        errorResponse.code mustEqual "INVALID_JSON"
        errorResponse.message mustEqual "Invalid JSON payload"
      }

      "return 400 BAD_REQUEST when issuingCountryTIN is longer than 2 characters" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val result = Await.result(amendRequest.withBody(invalidIssuingCountryTINJson).execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
        result.json.as[ORNErrorResponse].code mustEqual "INVALID_JSON"
        result.json.as[ORNErrorResponse].message mustEqual "Invalid JSON payload"
      }

      "return 400 BAD_REQUEST when reportingEntityName is empty" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val result = Await.result(amendRequest.withBody(emptyReportingEntityNameJson).execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
        result.json.as[ORNErrorResponse].code mustEqual "INVALID_JSON"
        result.json.as[ORNErrorResponse].message mustEqual "Invalid JSON payload"
      }

      "return 400 BAD_REQUEST when TIN is empty" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val result = Await.result(amendRequest.withBody(emptyTINJson).execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
        result.json.as[ORNErrorResponse].code mustEqual "INVALID_JSON"
        result.json.as[ORNErrorResponse].message mustEqual "Invalid JSON payload"
      }

      "return 400 BAD_REQUEST when reportingEntityName exceeds 200 characters" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val result = Await.result(amendRequest.withBody(longReportingEntityNameJson).execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
        result.json.as[ORNErrorResponse].code mustEqual "INVALID_JSON"
        result.json.as[ORNErrorResponse].message mustEqual "Invalid JSON payload"
      }

      "return 400 BAD_REQUEST when TIN exceeds 200 characters" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val result = Await.result(amendRequest.withBody(longTINJson).execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
        result.json.as[ORNErrorResponse].code mustEqual "INVALID_JSON"
        result.json.as[ORNErrorResponse].message mustEqual "Invalid JSON payload"
      }

      "return 200 OK for request with duplicate fields and additional fields" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )
        stubRequest(
          "PUT",
          amendUrl,
          OK,
          Json.toJson(ORNSuccessResponse("2022-01-31T09:26:17Z", "123456789012345"))
        )

        val result =
          Await.result(amendRequest.withBody(validRequestJson_duplicateFieldsAndAdditionalFields).execute[ORNSuccessResponse], 5.seconds)

        result.processingDate mustEqual "2022-01-31T09:26:17Z"
        result.formBundleNumber mustEqual "123456789012345"
      }

      "return 422 UNPROCESSABLE_ENTITY for invalid return from ETMP" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )
        stubRequest(
          "PUT",
          amendUrl,
          UNPROCESSABLE_ENTITY,
          Json.toJson(ORNErrorResponse("093", "Invalid Return"))
        )

        val result = Await.result(amendRequest.withBody(validRequestJson).execute[HttpResponse], 5.seconds)

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
          "PUT",
          amendUrl,
          UNAUTHORIZED,
          Json.toJson(ORNErrorResponse("500", "Internal Server Error"))
        )

        val result = Await.result(amendRequest.withBody(validRequestJson).execute[HttpResponse], 5.seconds)

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
          "PUT",
          amendUrl,
          INTERNAL_SERVER_ERROR,
          Json.toJson(ORNErrorResponse("500", "Internal Server Error"))
        )

        val result = Await.result(amendRequest.withBody(validRequestJson).execute[HttpResponse], 5.seconds)

        result.status mustEqual INTERNAL_SERVER_ERROR
        val errorResponse = result.json.as[ORNErrorResponse]
        errorResponse.code mustEqual "500"
        errorResponse.message mustEqual "Internal Server Error"
      }
    }

    "retrieveORN as an organisation" must {
      "return 200 OK when given valid period parameters" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val fromDate = "2023-01-01"
        val toDate   = "2024-12-31"

        val customResponse = retrieveOrnResponse.copy(
          accountingPeriodFrom = fromDate,
          accountingPeriodTo = toDate
        )

        stubGet(
          retrieveUrl(fromDate, toDate),
          OK,
          Json
            .toJson(
              Json.obj(
                "success" -> customResponse
              )
            )
            .toString()
        )

        val retrieveRequest = client
          .get(URI.create(s"http://localhost:$port${routes.OverseasReturnNotificationController.retrieveORN(fromDate, toDate).url}").toURL)
          .setHeader("X-Pillar2-Id" -> plrReference, "Authorization" -> "bearerToken")

        val result = Await.result(retrieveRequest.execute[ORNRetrieveSuccessResponse], 5.seconds)

        result.processingDate mustEqual customResponse.processingDate
        result.accountingPeriodFrom mustEqual fromDate
        result.accountingPeriodTo mustEqual toDate
        result.filedDateGIR mustEqual customResponse.filedDateGIR
        result.countryGIR mustEqual customResponse.countryGIR
        result.reportingEntityName mustEqual customResponse.reportingEntityName
        result.TIN mustEqual customResponse.TIN
        result.issuingCountryTIN mustEqual customResponse.issuingCountryTIN
      }

      "return 500 INTERNAL_SERVER_ERROR when ORN doesn't exist" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val fromDate = "2023-01-01"
        val toDate   = "2024-12-31"

        stubGet(
          retrieveUrl(fromDate, toDate),
          NOT_FOUND,
          Json.toJson(ORNErrorResponse("404", "Not Found")).toString()
        )

        val retrieveRequest = client
          .get(URI.create(s"http://localhost:$port${routes.OverseasReturnNotificationController.retrieveORN(fromDate, toDate).url}").toURL)
          .setHeader("X-Pillar2-Id" -> plrReference, "Authorization" -> "bearerToken")

        val result = Await.result(retrieveRequest.execute[HttpResponse], 5.seconds)

        result.status mustEqual INTERNAL_SERVER_ERROR
        val errorResponse = result.json.as[ORNErrorResponse]
        errorResponse.code mustEqual "500"
        errorResponse.message mustEqual "Internal Server Error"
      }

      "return 422 UNPROCESSABLE_ENTITY for invalid parameters" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val fromDate = "2023-01-01"
        val toDate   = "2024-12-31"

        stubGet(
          retrieveUrl(fromDate, toDate),
          UNPROCESSABLE_ENTITY,
          Json.toJson(ORNErrorResponse("093", "Invalid parameters")).toString()
        )

        val retrieveRequest = client
          .get(URI.create(s"http://localhost:$port${routes.OverseasReturnNotificationController.retrieveORN(fromDate, toDate).url}").toURL)
          .setHeader("X-Pillar2-Id" -> plrReference, "Authorization" -> "bearerToken")

        val result = Await.result(retrieveRequest.execute[HttpResponse], 5.seconds)

        result.status mustEqual UNPROCESSABLE_ENTITY
        val errorResponse = result.json.as[ORNErrorResponse]
        errorResponse.code mustEqual "093"
        errorResponse.message mustEqual "Invalid parameters"
      }

      "return 500 INTERNAL_SERVER_ERROR for internal server error from ETMP" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val fromDate = "2023-01-01"
        val toDate   = "2024-12-31"

        stubGet(
          retrieveUrl(fromDate, toDate),
          INTERNAL_SERVER_ERROR,
          Json.toJson(ORNErrorResponse("500", "Internal Server Error")).toString()
        )

        val retrieveRequest = client
          .get(URI.create(s"http://localhost:$port${routes.OverseasReturnNotificationController.retrieveORN(fromDate, toDate).url}").toURL)
          .setHeader("X-Pillar2-Id" -> plrReference, "Authorization" -> "bearerToken")

        val result = Await.result(retrieveRequest.execute[HttpResponse], 5.seconds)

        result.status mustEqual INTERNAL_SERVER_ERROR
        val errorResponse = result.json.as[ORNErrorResponse]
        errorResponse.code mustEqual "500"
        errorResponse.message mustEqual "Internal Server Error"
      }

      "return 500 INTERNAL_SERVER_ERROR when receiving malformed JSON on successful response" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val fromDate = "2023-01-01"
        val toDate   = "2024-12-31"

        stubGet(
          retrieveUrl(fromDate, toDate),
          OK,
          Json.obj("processingDate" -> "2022-01-31T09:26:17Z", "invalidField" -> "value").toString()
        )

        val retrieveRequest = client
          .get(URI.create(s"http://localhost:$port${routes.OverseasReturnNotificationController.retrieveORN(fromDate, toDate).url}").toURL)
          .setHeader("X-Pillar2-Id" -> plrReference, "Authorization" -> "bearerToken")

        val result = Await.result(retrieveRequest.execute[HttpResponse], 5.seconds)

        result.status mustEqual INTERNAL_SERVER_ERROR
        val errorResponse = result.json.as[ORNErrorResponse]
        errorResponse.code mustEqual "500"
        errorResponse.message mustEqual "Internal Server Error"
      }

      "return 500 INTERNAL_SERVER_ERROR when receiving malformed error JSON on 422 response" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val fromDate = "2023-01-01"
        val toDate   = "2024-12-31"

        stubGet(
          retrieveUrl(fromDate, toDate),
          UNPROCESSABLE_ENTITY,
          Json.obj("invalidErrorFormat" -> "value").toString()
        )

        val retrieveRequest = client
          .get(URI.create(s"http://localhost:$port${routes.OverseasReturnNotificationController.retrieveORN(fromDate, toDate).url}").toURL)
          .setHeader("X-Pillar2-Id" -> plrReference, "Authorization" -> "bearerToken")

        val result = Await.result(retrieveRequest.execute[HttpResponse], 5.seconds)

        result.status mustEqual INTERNAL_SERVER_ERROR
        val errorResponse = result.json.as[ORNErrorResponse]
        errorResponse.code mustEqual "500"
        errorResponse.message mustEqual "Internal Server Error"
      }
      "return 400 BAD_REQUEST when missing parameters" in {
        stubGet(
          "/report-pillar2-top-up-taxes/subscription/read-subscription/XCCVRUGFJG788",
          OK,
          Json.toJson(SubscriptionSuccess(subscriptionData)).toString()
        )

        val retrieveRequest = client
          .get(URI.create(s"http://localhost:$port/overseas-return-notification?accountingPeriodFrom=2024-01-01").toURL)
          .setHeader("X-Pillar2-Id" -> plrReference, "Authorization" -> "bearerToken")

        val result = Await.result(retrieveRequest.execute[HttpResponse], 5.seconds)

        result.status mustEqual BAD_REQUEST
        val errorResponse = result.json.as[ORNErrorResponse]
        errorResponse.code mustEqual "BAD_REQUEST"
        errorResponse.message mustEqual "Invalid request"
      }
    }

    "submitORN as an agent" must {
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
          Json.toJson(ORNSuccessResponse("2022-01-31T09:26:17Z", "123456789012345"))
        )

        val result =
          Await.result(
            submitRequest.withBody(validRequestJson).setHeader("X-Pillar2-Id" -> plrReference).execute[ORNSuccessResponse],
            5.seconds
          )

        result.processingDate mustEqual "2022-01-31T09:26:17Z"
        result.formBundleNumber mustEqual "123456789012345"
      }
    }

    "amendORN as an agent" must {
      "return 200 OK when given valid submission data" in {
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
          "PUT",
          amendUrl,
          OK,
          Json.toJson(ORNSuccessResponse("2022-01-31T09:26:17Z", "123456789012345"))
        )

        val result =
          Await.result(amendRequest.withBody(validRequestJson).setHeader("X-Pillar2-Id" -> plrReference).execute[ORNSuccessResponse], 5.seconds)

        result.processingDate mustEqual "2022-01-31T09:26:17Z"
        result.formBundleNumber mustEqual "123456789012345"
      }
    }

    "retrieveORN as an agent" must {
      "return 200 OK when given valid period parameters" in {
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

        val fromDate = "2023-01-01"
        val toDate   = "2024-12-31"

        val customResponse = retrieveOrnResponse.copy(
          accountingPeriodFrom = fromDate,
          accountingPeriodTo = toDate
        )

        stubGet(
          retrieveUrl(fromDate, toDate),
          OK,
          Json
            .toJson(
              Json.obj(
                "success" -> customResponse
              )
            )
            .toString()
        )

        val retrieveRequest = client
          .get(URI.create(s"http://localhost:$port${routes.OverseasReturnNotificationController.retrieveORN(fromDate, toDate).url}").toURL)
          .setHeader("X-Pillar2-Id" -> plrReference, "Authorization" -> "bearerToken")

        val result = Await.result(retrieveRequest.execute[ORNRetrieveSuccessResponse], 5.seconds)

        result.processingDate mustEqual customResponse.processingDate
        result.accountingPeriodFrom mustEqual fromDate
        result.accountingPeriodTo mustEqual toDate
        result.filedDateGIR mustEqual customResponse.filedDateGIR
        result.countryGIR mustEqual customResponse.countryGIR
        result.reportingEntityName mustEqual customResponse.reportingEntityName
        result.TIN mustEqual customResponse.TIN
        result.issuingCountryTIN mustEqual customResponse.issuingCountryTIN
      }
    }
  }
}

object OverseasReturnNotificationISpec {
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

  val invalidCountryGIRJson: JsValue =
    Json.parse("""{
                 |  "accountingPeriodFrom": "2023-01-01",
                 |  "accountingPeriodTo": "2024-12-31",
                 |  "filedDateGIR": "2024-12-31",
                 |  "countryGIR":"USA",
                 |  "reportingEntityName" : "Newco PLC",
                 |  "TIN" : "US12345678",
                 |  "issuingCountryTIN" : "US"
                 |}""".stripMargin)

  val invalidIssuingCountryTINJson: JsValue =
    Json.parse("""{
                 |  "accountingPeriodFrom": "2023-01-01",
                 |  "accountingPeriodTo": "2024-12-31",
                 |  "filedDateGIR": "2024-12-31",
                 |  "countryGIR":"US",
                 |  "reportingEntityName" : "Newco PLC",
                 |  "TIN" : "US12345678",
                 |  "issuingCountryTIN" : "USA"
                 |}""".stripMargin)

  val emptyReportingEntityNameJson: JsValue =
    Json.parse("""{
                 |  "accountingPeriodFrom": "2023-01-01",
                 |  "accountingPeriodTo": "2024-12-31",
                 |  "filedDateGIR": "2024-12-31",
                 |  "countryGIR":"US",
                 |  "reportingEntityName" : "",
                 |  "TIN" : "US12345678",
                 |  "issuingCountryTIN" : "US"
                 |}""".stripMargin)

  val emptyTINJson: JsValue =
    Json.parse("""{
                 |  "accountingPeriodFrom": "2023-01-01",
                 |  "accountingPeriodTo": "2024-12-31",
                 |  "filedDateGIR": "2024-12-31",
                 |  "countryGIR":"US",
                 |  "reportingEntityName" : "Newco PLC",
                 |  "TIN" : "",
                 |  "issuingCountryTIN" : "US"
                 |}""".stripMargin)

  val longReportingEntityNameJson: JsValue = {
    val longString = "a" * 201
    Json.parse(s"""{
                 |  "accountingPeriodFrom": "2023-01-01",
                 |  "accountingPeriodTo": "2024-12-31",
                 |  "filedDateGIR": "2024-12-31",
                 |  "countryGIR":"US",
                 |  "reportingEntityName" : "$longString",
                 |  "TIN" : "US12345678",
                 |  "issuingCountryTIN" : "US"
                 |}""".stripMargin)
  }

  val longTINJson: JsValue = {
    val longString = "a" * 201
    Json.parse(s"""{
                 |  "accountingPeriodFrom": "2023-01-01",
                 |  "accountingPeriodTo": "2024-12-31",
                 |  "filedDateGIR": "2024-12-31",
                 |  "countryGIR":"US",
                 |  "reportingEntityName" : "Newco PLC",
                 |  "TIN" : "$longString",
                 |  "issuingCountryTIN" : "US"
                 |}""".stripMargin)
  }

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
