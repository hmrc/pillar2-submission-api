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
import uk.gov.hmrc.pillar2submissionapi.helpers.SubscriptionDataFixture

class SubscriptionReadSpec extends AnyWordSpec with Matchers with SubscriptionDataFixture {

  "SubscriptionData (V1)" must {
    "successfully deserialise" when {
      "given a valid V1 payload (accountingPeriod as a single object)" in {
        v1Json.as[SubscriptionData] mustBe subscriptionData
      }

      "given a V1 payload with secondary contact and filing member" in {
        val v1JsonWithExtraParams = v1Json.as[JsObject] ++ Json.obj(
          "secondaryContactDetails" -> Json.obj(
            "name"         -> "Secondary Contact",
            "telephone"    -> "0987 6543 210",
            "emailAddress" -> "secondary.contact@example.com"
          ),
          "filingMemberDetails" -> Json.obj(
            "safeId"                  -> "XL6967739016188",
            "organisationName"        -> "Domestic Operations Ltd",
            "customerIdentification1" -> "2222222",
            "customerIdentification2" -> "3333333"
          )
        )

        val result = v1JsonWithExtraParams.as[SubscriptionData]
        result.secondaryContactDetails mustBe defined
        result.filingMemberDetails mustBe defined
      }
    }

    "successfully serialise" in {
      Json.toJson(subscriptionData) mustBe v1Json
    }

    "fail to deserialise" when {
      "given a V2 payload (accountingPeriod as an array)" in {
        v2Json.validate[SubscriptionData] mustBe a[JsError]
      }
    }

  }

  "SubscriptionDataV2" must {
    "successfully deserialise" when {
      "given a valid V2 payload (accountingPeriod as an array)" in {
        v2Json.as[SubscriptionDataV2] mustBe subscriptionDataV2
      }

      "given a V2 payload with no accounting period" in {
        val withoutPeriods = v2Json.as[JsObject] - "accountingPeriod"
        withoutPeriods.as[SubscriptionDataV2].accountingPeriod mustBe None
      }

      "given a V2 payload with accountingPeriod empty array" in {
        val withEmptyPeriods = v2Json.as[JsObject] + ("accountingPeriod" -> Json.arr())
        withEmptyPeriods.as[SubscriptionDataV2].accountingPeriod mustBe Some(Seq.empty)
      }
    }

    "successfully serialise" in {
      Json.toJson(subscriptionDataV2) mustBe v2Json
    }

    "fail to deserialise" when {
      "given a V1 payload (accountingPeriod as a single object)" in {
        v1Json.validate[SubscriptionDataV2] mustBe a[JsError]
      }
    }
  }

}
