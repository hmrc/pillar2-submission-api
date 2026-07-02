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

class SubmissionTypeSpec extends AnyWordSpec with Matchers {

  private val pillar2BackendSubmissionTypes = Seq(
    "UKTR_CREATE",
    "UKTR_AMEND",
    "ORN_CREATE",
    "ORN_AMEND",
    "BTN",
    "GIR_CREATE",
    "GIR_AMEND",
    "GIR_DELETE"
  )

  "SubmissionType" must
    pillar2BackendSubmissionTypes.foreach { value =>
      s"deserialise a submission of type $value" in {
        Json
          .parse(s"""{ "submissionType": "$value", "receivedDate": "2026-07-01T11:06:24Z" }""")
          .validate[Submission] mustBe a[JsSuccess[?]]
      }
    }

}
