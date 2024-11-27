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

package uk.gov.hmrc.pillar2submissionapi.controllers.actions

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.mvc.Results.Unauthorized
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.connectors.SubscriptionConnector
import uk.gov.hmrc.pillar2submissionapi.helpers.SubscriptionLocalDataFixture
import uk.gov.hmrc.pillar2submissionapi.models.requests.{IdentifierRequest, SubscriptionDataRequest}

import scala.concurrent.{ExecutionContext, Future}

class SubscriptionDataRetrievalActionSpec extends AnyWordSpec with SubscriptionLocalDataFixture {

  implicit lazy val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global //TODO: remove once SpecBase introduced

  class Harness(subscriptionConnector: SubscriptionConnector) extends SubscriptionDataRetrievalActionImpl(subscriptionConnector)(ec) {
    def callTransform[A](request: IdentifierRequest[A]): Future[SubscriptionDataRequest[A]] = transform(request)
  }

  "Subscription Data Retrieval Action" should {

    val mockSubscriptionConnector: SubscriptionConnector = mock[SubscriptionConnector]

    "build a SubscriptionLocalData object and add it to the request" in {

      when(mockSubscriptionConnector.getSubscriptionCache(any[String]())(any[HeaderCarrier](), any[ExecutionContext]())) thenReturn Future(
        Right(subscriptionLocalData)
      )
      val action = new Harness(mockSubscriptionConnector)

      val result = action
        .callTransform(IdentifierRequest(FakeRequest(), "id", Some("groupID"), userIdForEnrolment = "userId", clientPillar2Id = "pillar2Id"))
        .futureValue

      result.subscriptionLocalData mustBe Right(subscriptionLocalData)
    }

    "throw an exception when an error occurs while retrieving SubscriptionLocalData" in {

      when(mockSubscriptionConnector.getSubscriptionCache(any[String]())(any[HeaderCarrier](), any[ExecutionContext]())) thenReturn Future(
        Left(Unauthorized)
      )
      val action = new Harness(mockSubscriptionConnector)

      val result = action
        .callTransform(IdentifierRequest(FakeRequest(), "id", Some("groupID"), userIdForEnrolment = "userId", clientPillar2Id = "pillar2Id"))
        .futureValue

      result.subscriptionLocalData mustBe Left(Unauthorized)
    }
  }
}
