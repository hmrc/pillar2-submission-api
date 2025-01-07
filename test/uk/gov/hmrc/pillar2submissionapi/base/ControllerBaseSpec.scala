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

package uk.gov.hmrc.pillar2submissionapi.base

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.scalatest.Assertion
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc._
import play.api.test.Helpers.stubControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.pillar2submissionapi.connectors.SubscriptionConnector
import uk.gov.hmrc.pillar2submissionapi.controllers.actions.{AuthenticatedIdentifierAction, SubscriptionDataRetrievalAction}
import uk.gov.hmrc.pillar2submissionapi.helpers.{SubscriptionDataFixture, UKTaxReturnDataFixture}
import uk.gov.hmrc.pillar2submissionapi.models.requests.{IdentifierRequest, SubscriptionDataRequest}
import uk.gov.hmrc.pillar2submissionapi.services.{SubmitBTNService, UKTaxReturnService}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

trait ControllerBaseSpec extends PlaySpec with Results with Matchers with MockitoSugar with SubscriptionDataFixture with UKTaxReturnDataFixture {

  implicit lazy val ec:           ExecutionContext      = scala.concurrent.ExecutionContext.Implicits.global
  implicit lazy val system:       ActorSystem           = ActorSystem()
  implicit lazy val materializer: Materializer          = Materializer(system)
  implicit val cc:                ControllerComponents  = stubControllerComponents()
  val mockAuthConnector:          AuthConnector         = mock[AuthConnector]
  val mockSubscriptionConnector:  SubscriptionConnector = mock[SubscriptionConnector]

  val mockUkTaxReturnService: UKTaxReturnService = mock[UKTaxReturnService]
  val mockSubmitBTNService:   SubmitBTNService   = mock[SubmitBTNService]

  implicit val identifierAction: AuthenticatedIdentifierAction = new AuthenticatedIdentifierAction(
    mockAuthConnector,
    new BodyParsers.Default
  ) {
    override def transform[A](request: Request[A]): Future[IdentifierRequest[A]] =
      Future.successful(IdentifierRequest(request, "internalId", Some("groupID"), userIdForEnrolment = "userId", clientPillar2Id = ""))
  }

  val subscriptionDataRetrievalAction: SubscriptionDataRetrievalAction = new SubscriptionDataRetrievalAction {
    override protected def transform[A](request: IdentifierRequest[A]): Future[SubscriptionDataRequest[A]] =
      Future.successful(SubscriptionDataRequest(request, "internalId", "plrid", subscriptionData))

    override protected def executionContext: ExecutionContext = ec
  }

  implicit class AwaitFuture(fut: Future[Result]) {
    def shouldFailWith(expected: Throwable): Assertion = {
      val err = Await.result(fut.failed, 5.seconds)
      err shouldEqual expected
    }
  }

  implicit val subscriptionAction: SubscriptionDataRetrievalAction = new SubscriptionDataRetrievalAction {
    override protected def transform[A](request: IdentifierRequest[A]): Future[SubscriptionDataRequest[A]] =
      Future.successful(SubscriptionDataRequest(request, "internalId", "pillar2Id", subscriptionData))

    override protected def executionContext: ExecutionContext = ec
  }

}
