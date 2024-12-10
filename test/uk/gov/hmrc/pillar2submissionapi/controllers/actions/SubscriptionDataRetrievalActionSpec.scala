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
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.connectors.SubscriptionConnector
import uk.gov.hmrc.pillar2submissionapi.controllers.actions.base.ActionBaseSpec
import uk.gov.hmrc.pillar2submissionapi.controllers.error.NoSubscriptionData
import uk.gov.hmrc.pillar2submissionapi.helpers.SubscriptionDataFixture
import uk.gov.hmrc.pillar2submissionapi.models.requests.{IdentifierRequest, SubscriptionDataRequest}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

class SubscriptionDataRetrievalActionSpec extends ActionBaseSpec with SubscriptionDataFixture {

  class Harness(subscriptionConnector: SubscriptionConnector) extends SubscriptionDataRetrievalActionImpl(subscriptionConnector)(ec) {
    def callTransform[A](request: IdentifierRequest[A]): Future[SubscriptionDataRequest[A]] = transform(request)
  }

  "Subscription Data Retrieval Action" should {

    "build a SubscriptionData object and add it to the request" in {

      when(mockSubscriptionConnector.readSubscription(any[String]())(any[HeaderCarrier](), any[ExecutionContext]())) thenReturn Future(
        Right(subscriptionData)
      )
      val action = new Harness(mockSubscriptionConnector)

      val result = action
        .callTransform(IdentifierRequest(FakeRequest(), "id", Some("groupID"), userIdForEnrolment = "userId", clientPillar2Id = "pillar2Id"))
        .futureValue

      result.subscriptionData mustBe subscriptionData
    }

    "return a BadRequest when an error occurs while retrieving SubscriptionData which results in a RuntimeException being thrown" in {

      when(mockSubscriptionConnector.readSubscription(any[String]())(any[HeaderCarrier](), any[ExecutionContext]())) thenReturn Future(
        Left(BadRequest)
      )
      val action = new Harness(mockSubscriptionConnector)

      val result = action
        .callTransform(IdentifierRequest(FakeRequest(), "id", Some("groupID"), userIdForEnrolment = "userId", clientPillar2Id = "pillar2Id"))

      intercept[NoSubscriptionData] {
        Await.result(result, 5.seconds)
      }
    }
  }
}
