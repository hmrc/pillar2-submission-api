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

package uk.gov.hmrc.pillar2submissionapi.connectors

import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.Status.{BAD_REQUEST, OK}
import uk.gov.hmrc.pillar2submissionapi.base.IntegrationSpecBase
import uk.gov.hmrc.pillar2submissionapi.connectors.SubscriptionConnectorSpec._
import uk.gov.hmrc.pillar2submissionapi.helpers.SubscriptionDataFixture

class SubscriptionConnectorSpec extends IntegrationSpecBase with SubscriptionDataFixture with EitherValues {

  "readSubscription" must {
    "return json when the backend has returned 200 OK with data" in {
      val subcriptionConntector = app.injector.instanceOf[SubscriptionConnector]
      stubGet(s"$readSubscriptionPath/$plrReference", OK, subscriptionSuccess.toString)
      val result = subcriptionConntector.readSubscription(plrReference).futureValue
      result.isRight mustBe true
      result.value mustBe subscriptionDataJson
    }

    "return a BadRequest when the backend has returned a response else than 200 status" in {
      val subscriptionConnector = app.injector.instanceOf[SubscriptionConnector]
      stubGet(s"$readSubscriptionPath/$plrReference", BAD_REQUEST, unsuccessfulResponseJson)
      val result = subscriptionConnector.readSubscription(plrReference).futureValue
      result.isLeft mustBe true
      result mustBe Left(BadRequest)
    }
  }
}

object SubscriptionConnectorSpec {
  private val unsuccessfulResponseJson = """{ "status": "error" }"""
}
