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

package uk.gov.hmrc.pillar2submissionapi.models.enrolments

import play.api.libs.json.{Json, Reads}
import uk.gov.hmrc.auth.core.EnrolmentIdentifier

/** Represents client assigned to users in the ES21 API response.
  */

object EnrolmentIdentifierReads {
  implicit val enrolmentIdentifierReads: Reads[EnrolmentIdentifier] = Json.reads[EnrolmentIdentifier]
}

case class AssignedClient(serviceName: String, identifier: EnrolmentIdentifier, friendlyName: Option[String], assignedTo: String)

object AssignedClient {
  import EnrolmentIdentifierReads._
  implicit val readsAssignedClient: Reads[AssignedClient] = Json.reads[AssignedClient]
}

case class GroupDelegatedEnrolment(clients: Seq[AssignedClient])

object GroupDelegatedEnrolment {
  implicit val readsGroupDelegatedEnrolments: Reads[GroupDelegatedEnrolment] = Json.reads[GroupDelegatedEnrolment]
}
