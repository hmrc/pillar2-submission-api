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
class ValidationRuleSpec extends AnyWordSpec with Matchers {
  "ValidationRule" should {
    "create simple validation rules" in {
      val rule = ValidationRule[String] { str =>
        if (str.nonEmpty) valid(str)
        else invalid(MandatoryFieldMissing("test"))
      }

      // Test valid case
      val validResult = rule.validate("test")
      validResult.isValid mustBe true
      validResult.toEither match {
        case Right(value) => value mustBe "test"
        case Left(_)      => fail("Expected validation to succeed")
      }

      // Test invalid case
      val invalidResult = rule.validate("")
      invalidResult.isInvalid mustBe true
      invalidResult.toEither match {
        case Left(errors) => errors.head mustBe MandatoryFieldMissing("test")
        case Right(_)     => fail("Expected validation to fail")
      }
    }

    "combine multiple rules" in {
      val nonEmptyRule = ValidationRule[String] { str =>
        if (str.nonEmpty) valid(str)
        else invalid(MandatoryFieldMissing("test"))
      }

      val maxLengthRule = ValidationRule[String] { str =>
        if (str.length <= 5) valid(str)
        else invalid(MaxLengthExceeded("test", 5))
      }

      val combined = ValidationRule.combine(nonEmptyRule, maxLengthRule)

      // Test valid case
      val validResult = combined.validate("test")
      validResult.isValid mustBe true
      validResult.toEither match {
        case Right(value) => value mustBe "test"
        case Left(_)      => fail("Expected validation to succeed")
      }

      // Test empty string case
      val emptyResult = combined.validate("")
      emptyResult.isInvalid mustBe true
      emptyResult.toEither match {
        case Left(errors) => errors.head mustBe MandatoryFieldMissing("test")
        case Right(_)     => fail("Expected validation to fail")
      }

      // Test too long string case
      val tooLongResult = combined.validate("toolong")
      tooLongResult.isInvalid mustBe true
      tooLongResult.toEither match {
        case Left(errors) => errors.head mustBe MaxLengthExceeded("test", 5)
        case Right(_)     => fail("Expected validation to fail")
      }
    }

    "accumulate all errors when combining rules" in {
      val rule1 = ValidationRule[String](_ => invalid(MandatoryFieldMissing("field1")))
      val rule2 = ValidationRule[String](_ => invalid(MandatoryFieldMissing("field2")))

      val combined = ValidationRule.combine(rule1, rule2)
      val result   = combined.validate("test")

      result.isInvalid mustBe true
      result.toEither match {
        case Left(errors) =>
          errors.length mustBe 2
          errors.toList must contain(MandatoryFieldMissing("field1"))
          errors.toList must contain(MandatoryFieldMissing("field2"))

        case Right(_) => fail("Expected validation to fail")
      }
    }

  }
}
