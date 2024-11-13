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

package uk.gov.hmrc.pillar2submissionapi.validation

import cats.implicits._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.pillar2submissionapi.validation.ValidationResult._
import uk.gov.hmrc.pillar2submissionapi.validation.models.TestValidationError._

class ValidationResultSpec extends AnyWordSpec with Matchers {
  "ValidationResult" should {
    "create valid results" in {
      val result = valid("test")
      result.isValid mustBe true
    }

    "create invalid results" in {
      val error  = MandatoryFieldMissing("testField")
      val result = invalid[String](error)
      result.isInvalid mustBe true
    }

    "sequence multiple results" in {
      val results = Seq(
        valid("test1"),
        valid("test2")
      )

      val sequenced = sequence(results)
      sequenced.isValid mustBe true
      sequenced.toOption.get must contain theSameElementsAs Seq("test1", "test2")
    }

    "sequence multiple results with some errors" in {
      val results = Seq(
        valid("test1"),
        invalid(MandatoryFieldMissing("test2"))
      )

      val sequenced = sequence(results)
      sequenced.isInvalid mustBe true
      sequenced.toEither match {
        case Left(errors) =>
          errors.length mustBe 1
          errors.head mustBe MandatoryFieldMissing("test2")
        case Right(_) => fail("Expected invalid result")
      }
    }

    "accumulate multiple errors when sequencing" in {
      val results = Seq(
        invalid[String](MandatoryFieldMissing("field1")),
        invalid[String](MandatoryFieldMissing("field2"))
      )

      val sequenced = sequence(results)
      sequenced.isInvalid mustBe true
      sequenced.toEither match {
        case Left(errors) =>
          errors.length mustBe 2
          errors.toList must contain(MandatoryFieldMissing("field1"))
          errors.toList must contain(MandatoryFieldMissing("field2"))
        case Right(_) => fail("Expected invalid result")
      }
    }
  }
}
