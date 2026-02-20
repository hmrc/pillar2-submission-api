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

package uk.gov.hmrc.pillar2submissionapi.models.hip

import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.pillar2submissionapi.base.UnitTestBaseSpec

import java.time.ZonedDateTime

class ApiFailureResponseSpec extends UnitTestBaseSpec {
  "ApiFailureResponse" should {
    "serialize and deserialize to JSON correctly" in {
      val etmpDate = ZonedDateTime.parse("2024-03-14T09:26:17Z")
      val model    = ApiFailureResponse(ApiFailure(etmpDate, "093", "Invalid Message"))

      val json = Json.parse(
        """
          |{
          |  "errors": {
          |    "processingDate": "2024-03-14T09:26:17Z",
          |    "code": "093",
          |    "text": "Invalid Message"
          |  }
          |}
          |""".stripMargin
      )

      Json.toJson(model) mustEqual json
      json.validate[ApiFailureResponse] mustEqual JsSuccess(model)
    }
  }
}
