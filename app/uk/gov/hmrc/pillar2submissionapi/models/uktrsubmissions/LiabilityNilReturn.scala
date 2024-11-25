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

package uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions

import play.api.libs.json._

case class LiabilityNilReturn(returnType: ReturnType) extends Liability

object LiabilityNilReturn {

  implicit val reads: Reads[LiabilityNilReturn] =
    (JsPath \ "returnType").read[ReturnType].collect(JsonValidationError("returnType must be NIL_RETURN")) { case ReturnType.NIL_RETURN =>
      LiabilityNilReturn(ReturnType.NIL_RETURN)
    }

  implicit val writes: Writes[LiabilityNilReturn] = Json.writes[LiabilityNilReturn]
}
