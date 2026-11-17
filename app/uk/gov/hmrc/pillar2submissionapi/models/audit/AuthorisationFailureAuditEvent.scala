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

package uk.gov.hmrc.pillar2submissionapi.models.audit

import play.api.libs.json.{JsValue, Json, OWrites}

final case class AuthDetails(
  internalId:         Option[String],
  groupId:            Option[String],
  credId:             Option[String],
  affinityGroup:      Option[String],
  credentialRole:     Option[String],
  enrolmentPillar2Id: Option[String],
  enrolments:         Seq[String]
)

object AuthDetails {
  given writes: OWrites[AuthDetails] = Json.writes[AuthDetails]
}

final case class AuthorisationFailureAuditEvent(
  pillar2Id: String,
  errorCode: String,
  reason:    Option[String],
  user:      Option[AuthDetails]
) extends AuditEvent {
  override val auditType:  String  = "Pillar2ApiAuthorisationFailure"
  override val detailJson: JsValue = Json.toJson(this)
}

object AuthorisationFailureAuditEvent {
  given writes: OWrites[AuthorisationFailureAuditEvent] = Json.writes[AuthorisationFailureAuditEvent]
}
