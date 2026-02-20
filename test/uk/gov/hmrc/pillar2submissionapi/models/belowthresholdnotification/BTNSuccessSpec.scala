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

package uk.gov.hmrc.pillar2submissionapi.models.belowthresholdnotification

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._

import java.time.ZonedDateTime

class BTNSuccessSpec extends AnyWordSpec with Matchers {

  "BTNSuccess" should {
    "serialize to JSON correctly" in {
      val date         = ZonedDateTime.parse("2026-02-20T12:00:00Z")
      val model        = BTNSuccess(date)
      val expectedJson = Json.obj(
        "processingDate" -> "2026-02-20T12:00:00Z"
      )

      Json.toJson(model) shouldBe expectedJson
    }

    "deserialize from JSON correctly" in {
      val date = ZonedDateTime.parse("2026-02-20T12:00:00Z")
      val json = Json.obj(
        "processingDate" -> "2026-02-20T12:00:00Z"
      )

      json.as[BTNSuccess] shouldBe BTNSuccess(date)
    }
  }
}
