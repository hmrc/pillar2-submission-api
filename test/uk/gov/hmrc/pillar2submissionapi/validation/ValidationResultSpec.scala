package uk.gov.hmrc.pillar2submissionapi.validation

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import cats.data.NonEmptyChain
import uk.gov.hmrc.pillar2submissionapi.validation.ValidationError._
import uk.gov.hmrc.pillar2submissionapi.validation.ValidationResult._

class ValidationResultSpec extends AnyWordSpec with Matchers {
  "ValidationResult" should {
    "create valid results" in {
      val result = valid("test")
      result.isValid mustBe true
    }

    "create invalid results" in {
      val error = MandatoryFieldMissing("testField")
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
      sequenced.fold(
        errors => errors.length mustBe 1,
        _ => fail("Expected invalid result")
      )
    }

    "accumulate multiple errors when sequencing" in {
      val results = Seq(
        invalid[String](MandatoryFieldMissing("field1")),
        invalid[String](MandatoryFieldMissing("field2"))
      )
      
      val sequenced = sequence(results)
      sequenced.isInvalid mustBe true
      sequenced.fold(
        errors => errors.length mustBe 2,
        _ => fail("Expected invalid result")
      )
    }
  }
} 