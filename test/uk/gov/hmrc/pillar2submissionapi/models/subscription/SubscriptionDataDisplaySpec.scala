/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.pillar2submissionapi.models.subscription

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, JsObject, Json}
import uk.gov.hmrc.pillar2submissionapi.fixtures.SubscriptionDataFixtures

class SubscriptionDataDisplaySpec extends AnyWordSpec with Matchers with SubscriptionDataFixtures {

  "SubscriptionDataDisplay" must {
    "successfully deserialise" when {
      "given a valid payload (accountingPeriod as an array)" in {
        subscriptionDataDisplayJson.as[SubscriptionDataDisplay] mustBe subscriptionData
      }

      "given a payload with no accounting period" in {
        val withoutPeriods = subscriptionDataDisplayJson.as[JsObject] - "accountingPeriod"
        withoutPeriods.as[SubscriptionDataDisplay].accountingPeriod mustBe None
      }

      "given a payload with accountingPeriod empty array" in {
        val withEmptyPeriods = subscriptionDataDisplayJson.as[JsObject] + ("accountingPeriod" -> Json.arr())
        withEmptyPeriods.as[SubscriptionDataDisplay].accountingPeriod mustBe Some(Seq.empty)
      }
    }

    "successfully serialise" in {
      Json.toJson(subscriptionData) mustBe subscriptionDataDisplayJson
    }

    "fail to deserialise" when {
      "given a invalid payload" in {
        invalidSubscriptionJson.validate[SubscriptionDataDisplay] mustBe a[JsError]
      }
    }
  }

}
