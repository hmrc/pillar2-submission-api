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

import play.api.libs.json._
import uk.gov.hmrc.pillar2submissionapi.models.WrappedValue

case class IdType(value: String) extends WrappedValue[String]

object IdType {
  private val minLength = 1
  private val maxLength = 4

  private val reads: Reads[IdType] = Reads
    .of[String]
    .map(IdType.apply)
    .filter(JsonValidationError(s"IdType must be between $minLength and $maxLength characters")) { idType =>
      val length = idType.value.length
      length >= minLength && length <= maxLength
    }

  private val writes: Writes[IdType] = Writes(wrapped => Json.toJson(wrapped.value))

  implicit val format: Format[IdType] = Format(reads, writes)
}
