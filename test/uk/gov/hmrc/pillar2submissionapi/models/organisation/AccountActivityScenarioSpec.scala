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

package uk.gov.hmrc.pillar2submissionapi.models.organisation

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._

class AccountActivityScenarioSpec extends AnyWordSpec with Matchers {

  "AccountActivityScenario" when {
    "reading from JSON" must {
      "successfully parse valid scenario values" in {
        JsString("DTT_CHARGE").as[AccountActivityScenario] mustBe AccountActivityScenario.DTT_CHARGE
        JsString("REPAYMENT_CREDIT").as[AccountActivityScenario] mustBe AccountActivityScenario.REPAYMENT_CREDIT
        JsString("ACCRUED_INTEREST").as[AccountActivityScenario] mustBe AccountActivityScenario.ACCRUED_INTEREST
      }

      "return JsError for invalid scenario values" in {
        val result = JsString("INVALID_SCENARIO").validate[AccountActivityScenario]
        result mustBe a[JsError]
        result.asInstanceOf[JsError].errors.head._2.head.message must include("Invalid accountActivityScenario")
      }

      "include list of valid values in error message" in {
        val result = JsString("INVALID_SCENARIO").validate[AccountActivityScenario]
        result.asInstanceOf[JsError].errors.head._2.head.message must include("DTT_CHARGE")
      }
    }

    "writing to JSON" must {
      "serialize scenario to its string representation" in {
        Json.toJson(AccountActivityScenario.DTT_CHARGE) mustBe JsString("DTT_CHARGE")
        Json.toJson(AccountActivityScenario.REPAYMENT_CREDIT) mustBe JsString("REPAYMENT_CREDIT")
      }
    }
  }

  "TestData" when {
    "parsing JSON with accountActivityScenario" must {
      "successfully parse valid scenario" in {
        val json   = Json.obj("accountActivityScenario" -> "DTT_CHARGE")
        val result = json.validate[TestData]
        result mustBe a[JsSuccess[TestData]]
        result.get.accountActivityScenario mustBe AccountActivityScenario.DTT_CHARGE
      }

      "return JsError for null scenario" in {
        val json   = Json.obj("accountActivityScenario" -> JsNull)
        val result = json.validate[TestData]
        result mustBe a[JsError]
      }

      "return JsError for missing scenario" in {
        val json   = Json.obj()
        val result = json.validate[TestData]
        result mustBe a[JsError]
      }

      "return JsError for invalid scenario" in {
        val json   = Json.obj("accountActivityScenario" -> "INVALID_SCENARIO")
        val result = json.validate[TestData]
        result mustBe a[JsError]
      }
    }
  }
}
