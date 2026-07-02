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

package uk.gov.hmrc.pillar2submissionapi.models.obligationsandsubmissions

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsSuccess, Json}

class ObligationsAndSubmissionsSuccessResponseSpec extends AnyWordSpec with Matchers {

  "ObligationsAndSubmissionsSuccessResponse" must {

    "successfully deserialise" when {

      "a fulfilled obligation carries a GIR_CREATE submission" in {
        val json = Json.parse(
          """
            |{
            |  "processingDate": "2026-07-01T23:51:25Z",
            |  "accountingPeriodDetails": [
            |    {
            |      "startDate": "2024-01-01",
            |      "endDate": "2024-12-31",
            |      "dueDate": "2026-06-30",
            |      "underEnquiry": false,
            |      "obligations": [
            |        {
            |          "obligationType": "GIR",
            |          "status": "Fulfilled",
            |          "canAmend": true,
            |          "submissions": [
            |            { "submissionType": "GIR_CREATE", "receivedDate": "2026-07-01T11:06:24Z", "country": "GB" }
            |          ]
            |        }
            |      ]
            |    }
            |  ]
            |}""".stripMargin
        )

        json.validate[ObligationsAndSubmissionsSuccessResponse] mustBe a[JsSuccess[?]]
      }

      "a fulfilled obligation carries a GIR_AMEND submission" in {
        val json = Json.parse(
          """
            |{
            |  "processingDate": "2026-07-01T23:51:25Z",
            |  "accountingPeriodDetails": [
            |    {
            |      "startDate": "2024-01-01",
            |      "endDate": "2024-12-31",
            |      "dueDate": "2026-06-30",
            |      "underEnquiry": false,
            |      "obligations": [
            |        {
            |          "obligationType": "GIR",
            |          "status": "Fulfilled",
            |          "canAmend": true,
            |          "submissions": [
            |            { "submissionType": "GIR_AMEND", "receivedDate": "2026-07-01T11:06:24Z", "country": "GB" }
            |          ]
            |        }
            |      ]
            |    }
            |  ]
            |}""".stripMargin
        )

        json.validate[ObligationsAndSubmissionsSuccessResponse] mustBe a[JsSuccess[?]]
      }

      "a fulfilled obligation carries a GIR_DELETE submission" in {
        val json = Json.parse(
          """
            |{
            |  "processingDate": "2026-07-01T23:51:25Z",
            |  "accountingPeriodDetails": [
            |    {
            |      "startDate": "2024-01-01",
            |      "endDate": "2024-12-31",
            |      "dueDate": "2026-06-30",
            |      "underEnquiry": false,
            |      "obligations": [
            |        {
            |          "obligationType": "GIR",
            |          "status": "Fulfilled",
            |          "canAmend": true,
            |          "submissions": [
            |            { "submissionType": "GIR_DELETE", "receivedDate": "2026-07-01T11:06:24Z" }
            |          ]
            |        }
            |      ]
            |    }
            |  ]
            |}""".stripMargin
        )

        json.validate[ObligationsAndSubmissionsSuccessResponse] mustBe a[JsSuccess[?]]
      }
    }
  }

}
