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
import play.api.libs.json.{Format, Reads, Writes}
import uk.gov.hmrc.pillar2submissionapi.models.WrappedValue

case class LiableEntities(value: NonEmptyList[LiableEntity]) extends WrappedValue[NonEmptyList[LiableEntity]]

object LiableEntities {
  private val reads: Reads[LiableEntities] = Reads.list[LiableEntity].flatMap {
    case Nil        => Reads.failed("liableEntities must not be empty")
    case nonNil @ _ => Reads.pure(LiableEntities(NonEmptyList.fromListUnsafe(nonNil)))
  }

  private val writes: Writes[LiableEntities] = Writes.list[LiableEntity].contramap(_.value.toList)

  implicit val format: Format[LiableEntities] = Format(reads, writes)
}

