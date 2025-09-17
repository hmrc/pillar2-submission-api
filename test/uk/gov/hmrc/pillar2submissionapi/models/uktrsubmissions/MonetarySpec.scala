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

import scala.math.BigDecimal

class MonetarySpec extends AnyWordSpec with Matchers {

  "Monetary" should {
    "provide access to the wrapped value" in {
      val monetary = Monetary(BigDecimal("100.50"))
      monetary.value shouldBe BigDecimal("100.50")
    }

    "extend WrappedValue trait" in {
      val monetary = Monetary(BigDecimal("50.00"))
      monetary shouldBe a[WrappedValue[_]]
    }
  }

  "Monetary JSON format" should {
    "serialize correctly" in {
      val monetary = Monetary(BigDecimal("123.45"))
      val json     = Json.toJson(monetary)
      json shouldBe JsNumber(BigDecimal("123.45"))
    }

    "deserialize correctly" in {
      val json     = Json.parse("67.89")
      val monetary = Json.fromJson[Monetary](json).get
      monetary.value shouldBe BigDecimal("67.89")
    }

    "handle zero values" in {
      val json     = Json.parse("0")
      val monetary = Json.fromJson[Monetary](json).get
      monetary.value shouldBe BigDecimal("0")
    }

    "handle maximum valid values" in {
      val json     = Json.parse("9999999999999.99")
      val monetary = Json.fromJson[Monetary](json).get
      monetary.value shouldBe BigDecimal("9999999999999.99")
    }

    "handle values with 2 decimal places" in {
      val json     = Json.parse("123.45")
      val monetary = Json.fromJson[Monetary](json).get
      monetary.value shouldBe BigDecimal("123.45")
    }

    "handle values with 1 decimal place" in {
      val json     = Json.parse("123.4")
      val monetary = Json.fromJson[Monetary](json).get
      monetary.value shouldBe BigDecimal("123.4")
    }

    "handle values with no decimal places" in {
      val json     = Json.parse("123")
      val monetary = Json.fromJson[Monetary](json).get
      monetary.value shouldBe BigDecimal("123")
    }
  }

  "Monetary validation" should {
    "reject negative values" in {
      val json   = Json.parse("-100")
      val result = Json.fromJson[Monetary](json)
      result.isError shouldBe true
    }

    "reject values below minimum bound" in {
      val json   = Json.parse("-0.01")
      val result = Json.fromJson[Monetary](json)
      result.isError shouldBe true
    }

    "reject values above maximum bound" in {
      val json   = Json.parse("10000000000000")
      val result = Json.fromJson[Monetary](json)
      result.isError shouldBe true
    }

    "reject values with more than 2 decimal places" in {
      val json   = Json.parse("123.456")
      val result = Json.fromJson[Monetary](json)
      result.isError shouldBe true
    }

    "reject values with many decimal places" in {
      val json   = Json.parse("123.456789")
      val result = Json.fromJson[Monetary](json)
      result.isError shouldBe true
    }

    "accept boundary values" in {
      val minValue = Json.parse("0")
      val maxValue = Json.parse("9999999999999.99")

      Json.fromJson[Monetary](minValue).isSuccess shouldBe true
      Json.fromJson[Monetary](maxValue).isSuccess shouldBe true
    }

    "accept values with exactly 2 decimal places" in {
      val json   = Json.parse("9999999999999.99")
      val result = Json.fromJson[Monetary](json)
      result.isSuccess shouldBe true
    }
  }

  "Monetary edge cases" should {
    "handle very small positive values" in {
      val json     = Json.parse("0.01")
      val monetary = Json.fromJson[Monetary](json).get
      monetary.value shouldBe BigDecimal("0.01")
    }

    "handle large values with 2 decimal places" in {
      val json     = Json.parse("9999999999999.98")
      val monetary = Json.fromJson[Monetary](json).get
      monetary.value shouldBe BigDecimal("9999999999999.98")
    }

    "reject invalid JSON types" in {
      val invalidJson = JsString("not a number")
      val result      = Json.fromJson[Monetary](invalidJson)
      result.isError shouldBe true
    }
  }
}
