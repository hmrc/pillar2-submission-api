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

import cats.data.NonEmptyList
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._
import uk.gov.hmrc.pillar2submissionapi.models.WrappedValue
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.Monetary

import scala.math.BigDecimal

class LiableEntitiesSpec extends AnyWordSpec with Matchers {

  // Helper to create a test LiableEntity
  def createTestEntity(name: String = "Test Entity"): LiableEntity = LiableEntity(
    ukChargeableEntityName = EntityName(name),
    idType = IdType("CRN"),
    idValue = IdValue("12345678"),
    amountOwedDTT = Monetary(BigDecimal("100.00")),
    amountOwedIIR = Monetary(BigDecimal("200.00")),
    amountOwedUTPR = Monetary(BigDecimal("300.00"))
  )

  "LiableEntities" should {
    "provide access to the wrapped value" in {
      val entity   = createTestEntity()
      val entities = LiableEntities(NonEmptyList.of(entity))
      entities.value shouldBe NonEmptyList.of(entity)
    }

    "extend WrappedValue trait" in {
      val entity   = createTestEntity()
      val entities = LiableEntities(NonEmptyList.of(entity))
      entities shouldBe a[WrappedValue[?]]
    }
  }

  "LiableEntities JSON format" should {
    "serialize correctly" in {
      val entity   = createTestEntity()
      val entities = LiableEntities(NonEmptyList.of(entity))
      val json     = Json.toJson(entities)

      json                 shouldBe a[JsArray]
      json.as[JsArray].value should have size 1
    }

    "deserialize correctly" in {
      val entitiesJson = Json.parse("""[
        {
          "ukChargeableEntityName": "Test Entity",
          "idType": "CRN",
          "idValue": "12345678",
          "amountOwedDTT": 100.00,
          "amountOwedIIR": 200.00,
          "amountOwedUTPR": 300.00
        }
      ]""")

      val entities = Json.fromJson[LiableEntities](entitiesJson).get
      entities.value.toList                              should have size 1
      entities.value.head.ukChargeableEntityName.value shouldBe "Test Entity"
      entities.value.head.idType.value                 shouldBe "CRN"
      entities.value.head.idValue.value                shouldBe "12345678"
      entities.value.head.amountOwedDTT.value          shouldBe BigDecimal("100.00")
      entities.value.head.amountOwedIIR.value          shouldBe BigDecimal("200.00")
      entities.value.head.amountOwedUTPR.value         shouldBe BigDecimal("300.00")
    }

    "handle multiple entities" in {
      val entity1  = createTestEntity("Entity 1")
      val entity2  = createTestEntity("Entity 2")
      val entities = LiableEntities(NonEmptyList.of(entity1, entity2))

      val json = Json.toJson(entities)
      json.as[JsArray].value should have size 2

      val deserialized = Json.fromJson[LiableEntities](json).get
      deserialized.value.toList should have size 2
    }
  }

  "LiableEntities validation" should {
    "reject empty lists" in {
      val emptyJson = JsArray(Seq.empty)
      val result    = Json.fromJson[LiableEntities](emptyJson)
      result.isError shouldBe true
    }

    "accept single entity" in {
      val entity           = createTestEntity()
      val entityJson       = Json.toJson(entity)
      val singleEntityJson = JsArray(Seq(entityJson))

      val result = Json.fromJson[LiableEntities](singleEntityJson)
      result.isSuccess      shouldBe true
      result.get.value.toList should have size 1
    }

    "accept multiple entities" in {
      val entity1 = createTestEntity("Entity 1")
      val entity2 = createTestEntity("Entity 2")
      val entity3 = createTestEntity("Entity 3")

      val entitiesJson = JsArray(
        Seq(
          Json.toJson(entity1),
          Json.toJson(entity2),
          Json.toJson(entity3)
        )
      )

      val result = Json.fromJson[LiableEntities](entitiesJson)
      result.isSuccess      shouldBe true
      result.get.value.toList should have size 3
    }
  }

  "LiableEntities edge cases" should {
    "handle complex entity data" in {
      val complexEntity = LiableEntity(
        ukChargeableEntityName = EntityName("Complex & Associates (UK) Ltd"),
        idType = IdType("UTR"),
        idValue = IdValue("123456789"),
        amountOwedDTT = Monetary(BigDecimal("9999999.99")),
        amountOwedIIR = Monetary(BigDecimal("0.01")),
        amountOwedUTPR = Monetary(BigDecimal("5000.50"))
      )

      val entities     = LiableEntities(NonEmptyList.of(complexEntity))
      val json         = Json.toJson(entities)
      val deserialized = Json.fromJson[LiableEntities](json).get

      deserialized.value.head shouldBe complexEntity
    }

    "reject invalid JSON types" in {
      val invalidJson = JsString("not an array")
      val result      = Json.fromJson[LiableEntities](invalidJson)
      result.isError shouldBe true
    }

    "reject null values" in {
      val invalidJson = JsNull
      val result      = Json.fromJson[LiableEntities](invalidJson)
      result.isError shouldBe true
    }

    "reject arrays with invalid entity data" in {
      val invalidEntityJson = JsObject(
        Map(
          "invalid" -> JsString("data")
        )
      )
      val invalidArray = JsArray(Seq(invalidEntityJson))

      val result = Json.fromJson[LiableEntities](invalidArray)
      result.isError shouldBe true
    }
  }
}
