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

package uk.gov.hmrc.pillar2submissionapi.models.btn

import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.pillar2submissionapi.base.UnitTestBaseSpec

import java.time.ZonedDateTime

class BTNSuccessResponseSpec extends UnitTestBaseSpec {
  "BTNSuccessResponse" should {
    "serialize and deserialize to JSON correctly" in {
      val etmpDate = ZonedDateTime.parse("2022-01-31T09:26:17Z")
      val model    = BTNSuccessResponse(BTNSuccess(etmpDate))

      val json = Json.parse(
        """
          |{
          |  "success": {
          |    "processingDate": "2022-01-31T09:26:17Z"
          |  }
          |}
          |""".stripMargin
      )

      Json.toJson(model) mustEqual json
      json.validate[BTNSuccessResponse] mustEqual JsSuccess(model)
    }
  }
}
