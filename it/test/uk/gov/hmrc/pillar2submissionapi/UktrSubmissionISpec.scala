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
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.UktrSubmissionISpec._
import uk.gov.hmrc.pillar2submissionapi.base.IntegrationSpecBase
import uk.gov.hmrc.pillar2submissionapi.controllers.error.Pillar2ErrorResponse
import uk.gov.hmrc.pillar2submissionapi.controllers.routes
import uk.gov.hmrc.play.bootstrap.http.HttpClientV2Provider

import java.net.URI
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

class UktrSubmissionISpec extends IntegrationSpecBase {

  val provider    = app.injector.instanceOf[HttpClientV2Provider]
  val client      = provider.get()
  val str         = s"http://localhost:$port${routes.UktrSubmissionController.submitUktr.url}"
  val baseRequest = client.post(URI.create(str).toURL)

  "UKTR Submission" when {
    "Subscription data exists" should {
      "Create a new UKTR submission (POST)" that {
        "has valid submission data" should {

          "return a 201 CREATED response" in {
            val request = baseRequest.withBody(validRequestJson)
            val result  = Await.result(request.execute[JsValue], 5.seconds)
            (result \ "success").as[Boolean] mustEqual true
          }
        }
        "has valid nil-return submission data" should {

          "return a 201 CREATED response" in {
            val request = baseRequest.withBody(validRequestNilReturnJson)
            val result  = Await.result(request.execute[JsValue], 5.seconds)
            (result \ "success").as[Boolean] mustEqual true
          }
        }
        "has an invalid request body" should {

          "return a 400 BAD_REQUEST response" in {
            val request = baseRequest.withBody(invalidRequestJson)
            val result  = Await.result(request.execute[HttpResponse], 5.seconds)
            result.status mustEqual 400
          }
        }
        "has an empty request body" should {

          "return a 400 BAD_REQUEST response" in {
            val request = baseRequest.withBody(JsObject.empty)
            val result  = Await.result(request.execute[HttpResponse], 5.seconds)
            result.status mustEqual 400
          }
        }
        "has no request body" should {
          "return a 400 BAD_REQUEST response " in {
            val request = baseRequest
            val result  = Await.result(request.execute[HttpResponse], 5.seconds)
            result.status mustEqual 400
          }
        }
      }

      "has a valid request body containing duplicates fields and additional fields" should {

        "return a 201 CREATED response" in {
          val request = baseRequest.withBody(validRequestJson_duplicateFieldsAndAdditionalFields)
          val result  = Await.result(request.execute[JsValue], 5.seconds)
          (result \ "success").as[Boolean] mustEqual true
        }
      }

      "Subscription data does not exist" should {
        "return a BadRequest resulting in a RuntimeException being thrown" in {
          val request = baseRequest.withBody(validRequestJson)
          when(mockSubscriptionConnector.readSubscription(any[String]())(any[HeaderCarrier](), any[ExecutionContext]()))
            .thenReturn(
              Future.successful(Left(BadRequest))
            )

          val result = Await.result(request.execute[HttpResponse], 5.seconds)
          result.status mustEqual 500
          val errorResponse = result.json.as[Pillar2ErrorResponse]
          errorResponse.code mustEqual "004"
          errorResponse.message mustEqual "No Pillar2 subscription found for XCCVRUGFJG788"
        }
      }
    }
  }
}

object UktrSubmissionISpec {
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
