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

case class EntityName(value: String) extends WrappedValue[String]

object EntityName {
  private val minLength = 1
  private val maxLength = 160

  private val reads: Reads[EntityName] = Reads
    .of[String]
    .map(EntityName.apply)
    .filter(JsonValidationError(s"EntityName must be between $minLength and $maxLength characters")) { entityName =>
      val length = entityName.value.length
      length >= minLength && length <= maxLength
    }

  private val writes: Writes[EntityName] = Writes(wrapped => Json.toJson(wrapped.value))

  implicit val format: Format[EntityName] = Format(reads, writes)
}
