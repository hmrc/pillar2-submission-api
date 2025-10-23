/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._
import uk.gov.hmrc.pillar2submissionapi.models.WrappedValue

class IdValueSpec extends AnyWordSpec with Matchers {

  "IdValue" should {
    "provide access to the wrapped value" in {
      val idValue = IdValue("12345678")
      idValue.value shouldBe "12345678"
    }

    "extend WrappedValue trait" in {
      val idValue = IdValue("ABCD1234")
      idValue shouldBe a[WrappedValue[?]]
    }
  }

  "IdValue JSON format" should {
    "serialize correctly" in {
      val idValue = IdValue("12345678")
      val json    = Json.toJson(idValue)
      json shouldBe JsString("12345678")
    }

    "deserialize correctly" in {
      val json    = Json.parse("\"87654321\"")
      val idValue = Json.fromJson[IdValue](json).get
      idValue.value shouldBe "87654321"
    }

    "handle single character values" in {
      val json    = Json.parse("\"A\"")
      val idValue = Json.fromJson[IdValue](json).get
      idValue.value shouldBe "A"
    }

    "handle maximum length values" in {
      val json    = Json.parse("\"123456789012345\"")
      val idValue = Json.fromJson[IdValue](json).get
      idValue.value shouldBe "123456789012345"
    }
  }

  "IdValue validation" should {
    "reject empty strings" in {
      val json   = Json.parse("\"\"")
      val result = Json.fromJson[IdValue](json)
      result.isError shouldBe true
    }

    "reject strings that are too long" in {
      val json   = Json.parse("\"1234567890123456\"")
      val result = Json.fromJson[IdValue](json)
      result.isError shouldBe true
    }

    "reject very long strings" in {
      val json   = Json.parse("\"This is a very long string that exceeds the maximum allowed length of 15 characters\"")
      val result = Json.fromJson[IdValue](json)
      result.isError shouldBe true
    }

    "accept boundary values" in {
      val minValue = Json.parse("\"A\"")
      val maxValue = Json.parse("\"123456789012345\"")

      Json.fromJson[IdValue](minValue).isSuccess shouldBe true
      Json.fromJson[IdValue](maxValue).isSuccess shouldBe true
    }

    "accept common ID value patterns" in {
      val commonValues = List(
        "12345678", // 8 chars
        "AB123456", // 8 chars with letters
        "123456789012345" // 15 chars (max)
      )

      commonValues.foreach { idValue =>
        val json   = Json.parse(s"\"$idValue\"")
        val result = Json.fromJson[IdValue](json)
        result.isSuccess shouldBe true
        result.get.value shouldBe idValue
      }
    }
  }

  "IdValue edge cases" should {
    "handle special characters" in {
      val json    = Json.parse("\"A-1_B2\"")
      val idValue = Json.fromJson[IdValue](json).get
      idValue.value shouldBe "A-1_B2"
    }

    "handle mixed alphanumeric values" in {
      val json    = Json.parse("\"ABC123DEF\"")
      val idValue = Json.fromJson[IdValue](json).get
      idValue.value shouldBe "ABC123DEF"
    }

    "handle numeric strings" in {
      val json    = Json.parse("\"123456789\"")
      val idValue = Json.fromJson[IdValue](json).get
      idValue.value shouldBe "123456789"
    }

    "reject invalid JSON types" in {
      val invalidJson = JsNumber(42)
      val result      = Json.fromJson[IdValue](invalidJson)
      result.isError shouldBe true
    }

    "reject null values" in {
      val invalidJson = JsNull
      val result      = Json.fromJson[IdValue](invalidJson)
      result.isError shouldBe true
    }
  }
}
