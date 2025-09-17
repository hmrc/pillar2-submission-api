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

class IdTypeSpec extends AnyWordSpec with Matchers {

  "IdType" should {
    "provide access to the wrapped value" in {
      val idType = IdType("CRN")
      idType.value shouldBe "CRN"
    }

    "extend WrappedValue trait" in {
      val idType = IdType("UTR")
      idType shouldBe a[WrappedValue[_]]
    }
  }

  "IdType JSON format" should {
    "serialize correctly" in {
      val idType = IdType("CRN")
      val json   = Json.toJson(idType)
      json shouldBe JsString("CRN")
    }

    "deserialize correctly" in {
      val json   = Json.parse("\"UTR\"")
      val idType = Json.fromJson[IdType](json).get
      idType.value shouldBe "UTR"
    }

    "handle single character values" in {
      val json   = Json.parse("\"A\"")
      val idType = Json.fromJson[IdType](json).get
      idType.value shouldBe "A"
    }

    "handle maximum length values" in {
      val json   = Json.parse("\"ABCD\"")
      val idType = Json.fromJson[IdType](json).get
      idType.value shouldBe "ABCD"
    }
  }

  "IdType validation" should {
    "reject empty strings" in {
      val json   = Json.parse("\"\"")
      val result = Json.fromJson[IdType](json)
      result.isError shouldBe true
    }

    "reject strings that are too long" in {
      val json   = Json.parse("\"ABCDE\"")
      val result = Json.fromJson[IdType](json)
      result.isError shouldBe true
    }

    "reject very long strings" in {
      val json   = Json.parse("\"ABCDEFGHIJKLMNOP\"")
      val result = Json.fromJson[IdType](json)
      result.isError shouldBe true
    }

    "accept boundary values" in {
      val minValue = Json.parse("\"A\"")
      val maxValue = Json.parse("\"ABCD\"")

      Json.fromJson[IdType](minValue).isSuccess shouldBe true
      Json.fromJson[IdType](maxValue).isSuccess shouldBe true
    }

    "accept common ID types" in {
      val commonTypes = List("CRN", "UTR", "NINO", "VAT")

      commonTypes.foreach { idType =>
        val json   = Json.parse(s"\"$idType\"")
        val result = Json.fromJson[IdType](json)
        result.isSuccess shouldBe true
        result.get.value shouldBe idType
      }
    }
  }

  "IdType edge cases" should {
    "handle special characters" in {
      val json   = Json.parse("\"A-1\"")
      val idType = Json.fromJson[IdType](json).get
      idType.value shouldBe "A-1"
    }

    "handle numbers in ID types" in {
      val json   = Json.parse("\"123\"")
      val idType = Json.fromJson[IdType](json).get
      idType.value shouldBe "123"
    }

    "reject invalid JSON types" in {
      val invalidJson = JsNumber(42)
      val result      = Json.fromJson[IdType](invalidJson)
      result.isError shouldBe true
    }

    "reject null values" in {
      val invalidJson = JsNull
      val result      = Json.fromJson[IdType](invalidJson)
      result.isError shouldBe true
    }
  }
}
