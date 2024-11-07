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

import enumeratum.EnumEntry.UpperSnakecase
import enumeratum._
import play.api.libs.json._

sealed trait ReturnType extends EnumEntry with UpperSnakecase

object ReturnType extends Enum[ReturnType] with PlayJsonEnum[ReturnType] {

  implicit val returnTypeFormat: Format[ReturnType] = Format(
    Reads[ReturnType] { json =>
      json.validate[String].flatMap {
        case "NIL_RETURN" => JsSuccess(ReturnType.NilReturn)
        case _ => JsError("INVALID")
      }
    },
    Writes[ReturnType] {
      case ReturnType.NilReturn => JsString("NIL_RETURN")
      case _ => JsString("INVALID")
    }
  )
  val values: IndexedSeq[ReturnType] = findValues

  case object NilReturn extends ReturnType
}
