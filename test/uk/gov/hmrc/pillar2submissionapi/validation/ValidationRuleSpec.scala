package uk.gov.hmrc.pillar2submissionapi.validation

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.pillar2submissionapi.validation.ValidationError._
import uk.gov.hmrc.pillar2submissionapi.validation.ValidationResult._

class ValidationRuleSpec extends AnyWordSpec with Matchers {
  "ValidationRule" should {
    "create simple validation rules" in {
      val rule = ValidationRule[String](str => 
        if (str.nonEmpty) valid(str)
        else invalid(MandatoryFieldMissing("test"))
      )
      
      rule.validate("test").isValid mustBe true
      rule.validate("").isInvalid mustBe true
    }

    "combine multiple rules" in {
      val rule1 = ValidationRule[String](str => 
        if (str.nonEmpty) valid(str)
        else invalid(MandatoryFieldMissing("test"))
      )
      
      val rule2 = ValidationRule[String](str => 
        if (str.length <= 5) valid(str)
        else invalid(MaxLengthExceeded("test", 5))
      )
      
      val combined = ValidationRule.combine(rule1, rule2)
      
      combined.validate("test").isValid mustBe true
      combined.validate("").isInvalid mustBe true
      combined.validate("toolong").isInvalid mustBe true
    }

    "accumulate all errors when combining rules" in {
      val rule1 = ValidationRule[String](_ => invalid(MandatoryFieldMissing("field1")))
      val rule2 = ValidationRule[String](_ => invalid(MandatoryFieldMissing("field2")))
      
      val combined = ValidationRule.combine(rule1, rule2)
      val result = combined.validate("test")
      
      
      result.fold(
        errors => errors.length mustBe 2,
        _ => fail("Expected invalid result")
      )
    }
  }
} 