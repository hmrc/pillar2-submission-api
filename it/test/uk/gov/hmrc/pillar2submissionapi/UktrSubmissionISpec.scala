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
import play.api.http.Status.{CREATED, INTERNAL_SERVER_ERROR, OK, UNAUTHORIZED, UNPROCESSABLE_ENTITY}
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.UKTRSubmissionISpec._
import uk.gov.hmrc.pillar2submissionapi.base.IntegrationSpecBase
import uk.gov.hmrc.pillar2submissionapi.controllers.error.{AuthenticationError, Pillar2ErrorResponse}
import uk.gov.hmrc.pillar2submissionapi.controllers.routes
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.responses.{SubmitUktrErrorResponse, SubmitUktrSuccessResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClientV2Provider

import java.net.URI
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

class UKTRSubmissionISpec extends IntegrationSpecBase with OptionValues {

  lazy val provider    = app.injector.instanceOf[HttpClientV2Provider]
  lazy val client      = provider.get()
  lazy val str         = s"http://localhost:$port${routes.UKTRSubmissionController.submitUktr.url}"
  lazy val baseRequest = client.post(URI.create(str).toURL)

  "Create a new UKTR submission (POST)" should "create submission when given valid submission data" in {
    stubGet(s"$readSubscriptionPath/$plrReference", OK, subscriptionSuccess.toString)
    stubResponse(
      "/submit-uk-tax-return",
      CREATED,
      Json.toJson(SubmitUktrSuccessResponse("2022-01-31T09:26:17Z", "119000004320", Some("XTC01234123412")))
    )
    val request = baseRequest.withBody(validRequestJson)
    val result  = Await.result(request.execute[SubmitUktrSuccessResponse], 5.seconds)
    result.chargeReference.value mustEqual "XTC01234123412"
    result.formBundleNumber mustEqual "119000004320"
  }

  "has valid nil-return submission data" should "return a 201 CREATED response" in {
    stubGet(s"$readSubscriptionPath/$plrReference", OK, subscriptionSuccess.toString)
    stubResponse(
      "/submit-uk-tax-return",
      CREATED,
      Json.toJson(SubmitUktrSuccessResponse("2022-01-31T09:26:17Z", "119000004320", Some("XTC01234123412")))
    )
    val request = baseRequest.withBody(validRequestNilReturnJson)
    val result  = Await.result(request.execute[SubmitUktrSuccessResponse], 5.seconds)
    result.chargeReference.value mustEqual "XTC01234123412"
    result.formBundleNumber mustEqual "119000004320"
  }

  "has an invalid request body" should "return a 400 BAD_REQUEST response" in {
    stubGet(s"$readSubscriptionPath/$plrReference", OK, subscriptionSuccess.toString)
    val request = baseRequest.withBody(invalidRequestJson)
    val result  = Await.result(request.execute[HttpResponse], 5.seconds)
    result.status mustEqual 400
  }

  "has an empty request body" should "return a 400 BAD_REQUEST response " in {
    stubGet(s"$readSubscriptionPath/$plrReference", OK, subscriptionSuccess.toString)
    val request = baseRequest.withBody(JsObject.empty)
    val result  = Await.result(request.execute[HttpResponse], 5.seconds)
    result.status mustEqual 400
  }

  "has no request body" should "return a 400 BAD_REQUEST response " in {
    stubGet(s"$readSubscriptionPath/$plrReference", OK, subscriptionSuccess.toString)
    val request = baseRequest
    val result  = Await.result(request.execute[HttpResponse], 5.seconds)
    result.status mustEqual 400
  }

  "has a valid request body containing duplicates fields and additional fields" should "return a 201 CREATED response" in {
    stubGet(s"$readSubscriptionPath/$plrReference", OK, subscriptionSuccess.toString)
    stubResponse(
      "/submit-uk-tax-return",
      CREATED,
      Json.toJson(SubmitUktrSuccessResponse("2022-01-31T09:26:17Z", "119000004320", Some("XTC01234123412")))
    )
    val request = baseRequest.withBody(validRequestJson_duplicateFieldsAndAdditionalFields)
    val result  = Await.result(request.execute[SubmitUktrSuccessResponse], 5.seconds)
    result.chargeReference.value mustEqual "XTC01234123412"
    result.formBundleNumber mustEqual "119000004320"
  }

  "Subscription data does not exist" should "return a InternalServerError resulting in a RuntimeException being thrown" in {
    val request = baseRequest.withBody(validRequestJson)

    val result = Await.result(request.execute[HttpResponse], 5.seconds)
    result.status mustEqual 500
    val errorResponse = result.json.as[Pillar2ErrorResponse]
    errorResponse.code mustEqual "004"
    errorResponse.message mustEqual "No Pillar2 subscription found for XCCVRUGFJG788"
  }

  "User unable to be identified" should "return a InternalServerError resulting in a RuntimeException being thrown" in {
    when(
      mockAuthConnector.authorise[RetrievalsType](any[Predicate](), any[Retrieval[RetrievalsType]]())(any[HeaderCarrier](), any[ExecutionContext]())
    )
      .thenReturn(
        Future.failed(AuthenticationError("Invalid credentials"))
      )
    val request = baseRequest.withBody(validRequestJson)

    val result = Await.result(request.execute[HttpResponse], 5.seconds)
    result.status mustEqual 401
    val errorResponse = result.json.as[Pillar2ErrorResponse]
    errorResponse.code mustEqual "003"
    errorResponse.message mustEqual "Invalid credentials"
  }

  "'Invalid Return' response from ETMP returned" should "return a 422 UNPROCESSABLE_ENTITY response" in {
    stubGet(s"$readSubscriptionPath/$plrReference", OK, subscriptionSuccess.toString)
    stubResponse(
      "/submit-uk-tax-return",
      UNPROCESSABLE_ENTITY,
      Json.toJson(SubmitUktrErrorResponse("093", "Invalid Return"))
    )
    val request = baseRequest.withBody(validRequestJson)
    val result  = Await.result(request.execute[HttpResponse], 5.seconds)
    result.status mustEqual 422
    val errorResponse = result.json.as[Pillar2ErrorResponse]
    errorResponse.code mustEqual "093"
    errorResponse.message mustEqual "Invalid Return"
  }

  "'Unauthorized' response from ETMP returned" should "return a 500 INTERNAL_SERVER_ERROR response" in {
    stubGet(s"$readSubscriptionPath/$plrReference", OK, subscriptionSuccess.toString)
    stubResponse(
      "/submit-uk-tax-return",
      UNAUTHORIZED,
      Json.toJson(SubmitUktrErrorResponse("001", "Unauthorized"))
    )
    val request = baseRequest.withBody(validRequestJson)
    val result  = Await.result(request.execute[HttpResponse], 5.seconds)
    result.status mustEqual 500
    val errorResponse = result.json.as[Pillar2ErrorResponse]
    errorResponse.code mustEqual "500"
    errorResponse.message mustEqual "Internal Server Error"
  }

  "'internal server error' response from ETMP returned" should "return a 500 INTERNAL_SERVER_ERROR response" in {
    stubGet(s"$readSubscriptionPath/$plrReference", OK, subscriptionSuccess.toString)
    stubResponse(
      "/submit-uk-tax-return",
      INTERNAL_SERVER_ERROR,
      Json.toJson(SubmitUktrErrorResponse("999", "internal_server_error"))
    )
    val request = baseRequest.withBody(validRequestJson)
    val result  = Await.result(request.execute[HttpResponse], 5.seconds)
    result.status mustEqual 500
    val errorResponse = result.json.as[Pillar2ErrorResponse]
    errorResponse.code mustEqual "500"
    errorResponse.message mustEqual "Internal Server Error"
  }
}

object UKTRSubmissionISpec {

  val validRequestJson: JsValue =
    Json.parse("""{
        |  "accountingPeriodFrom": "2024-08-14",
        |  "accountingPeriodTo": "2024-12-14",
        |  "obligationMTT": true,
        |  "electionUKGAAP": true,
        |  "liabilities": {
        |    "electionDTTSingleMember": false,
        |    "electionUTPRSingleMember": false,
        |    "numberSubGroupDTT": 1,
        |    "numberSubGroupUTPR": 1,
        |    "totalLiability": 10000.99,
        |    "totalLiabilityDTT": 5000.99,
        |    "totalLiabilityIIR": 4000,
        |    "totalLiabilityUTPR": 10000.99,
        |    "liableEntities": [
        |      {
        |        "ukChargeableEntityName": "Newco PLC",
        |        "idType": "CRN",
        |        "idValue": "12345678",
        |        "amountOwedDTT": 5000,
        |        "amountOwedIIR": 3400,
        |        "amountOwedUTPR": 6000.5
        |      }
        |    ]
        |  }
        |}""".stripMargin)

  val validRequestNilReturnJson: JsValue =
    Json.parse("""{
        |  "accountingPeriodFrom": "2024-08-14",
        |  "accountingPeriodTo": "2024-09-14",
        |  "obligationMTT": true,
        |  "electionUKGAAP": true,
        |  "liabilities": {
        |    "returnType": "NIL_RETURN"
        |  }
        |}
        |""".stripMargin)

  val invalidRequestJson: JsValue =
    Json.parse("""{
        |  "badRequest": ""
        |}""".stripMargin)

  val validRequestJson_duplicateFieldsAndAdditionalFields: JsValue =
    Json.parse("""{
        |  "accountingPeriodFrom": "2024-08-14",
        |  "accountingPeriodTo": "2024-12-14",
        |  "obligationMTT": true,
        |  "obligationMTT": true,
        |  "electionUKGAAP": true,
        |  "extraField": "this should not be here",
        |  "liabilities": {
        |    "electionDTTSingleMember": false,
        |    "electionUTPRSingleMember": false,
        |    "numberSubGroupDTT": 1,
        |    "numberSubGroupUTPR": 1,
        |    "totalLiability": 10000.99,
        |    "totalLiabilityDTT": 5000.99,
        |    "totalLiabilityIIR": 4000,
        |    "totalLiabilityUTPR": 10000.99,
        |    "totalLiabilityUTPR": 10000.99,
        |    "liableEntities": [
        |      {
        |        "ukChargeableEntityName": "Newco PLC",
        |        "idType": "CRN",
        |        "idValue": "12345678",
        |        "amountOwedDTT": 5000,
        |        "amountOwedIIR": 3400,
        |        "amountOwedUTPR": 6000.5
        |      },
        |      {
        |        "ukChargeableEntityName": "Newco PLC",
        |        "idType": "CRN",
        |        "idValue": "12345678",
        |        "amountOwedDTT": 5000,
        |        "amountOwedIIR": 3400,
        |        "amountOwedUTPR": 6000.5
        |      }
        |    ]
        |  }
        |}""".stripMargin)
}
