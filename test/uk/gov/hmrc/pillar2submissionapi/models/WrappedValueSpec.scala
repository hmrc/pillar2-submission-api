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

package uk.gov.hmrc.pillar2submissionapi.models

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._

class WrappedValueSpec extends AnyWordSpec with Matchers {

  // Test case class that extends WrappedValue
  case class TestValue(value: String) extends WrappedValue[String]

  object TestValue {
    given format: Format[TestValue] = WrappedValueFormat[String, TestValue](TestValue.apply)
  }

  "WrappedValue" should {
    "provide access to the wrapped value" in {
      val testValue = TestValue("test")
      testValue.value shouldBe "test"
    }
  }

  "WrappedValueFormat" should {
    "create a format that can serialize and deserialize correctly" in {
      val testValue = TestValue("hello world")

      // Test serialization
      val json = Json.toJson(testValue)
      json shouldBe JsString("hello world")

      // Test deserialization
      val rawJson      = Json.parse("\"hello world\"")
      val deserialized = Json.fromJson[TestValue](rawJson)
      deserialized.get shouldBe testValue
    }

    "handle JSON parsing errors gracefully" in {
      val invalidJson = JsNumber(42)

      val result = Json.fromJson[TestValue](invalidJson)
      result.isError shouldBe true
    }

    "work with different underlying types" in {
      case class IntValue(value: Int) extends WrappedValue[Int]
      object IntValue {
        given format: Format[IntValue] = WrappedValueFormat[Int, IntValue](IntValue.apply)
      }

      val intValue = IntValue(42)
      val json     = Json.toJson(intValue)
      json shouldBe JsNumber(42)

      val rawJson      = Json.parse("42")
      val deserialized = Json.fromJson[IntValue](rawJson)
      deserialized.get shouldBe intValue
    }
  }
}
