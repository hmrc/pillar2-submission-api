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

package uk.gov.hmrc.pillar2submissionapi.services

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.base.UnitTestBaseSpec
import uk.gov.hmrc.pillar2submissionapi.models.audit.{AuthDetails, AuthorisationFailureAuditEvent}
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import scala.concurrent.{ExecutionContext, Future}

class AuditServiceSpec extends UnitTestBaseSpec {

  private val mockAuditConnector: AuditConnector = mock[AuditConnector]
  private val auditService:       AuditService   = new AuditService(mockAuditConnector)

  "auditAuthorisationFailure" should {
    "send a Pillar2ApiAuthorisationFailure event with the details of the rejected request" in {
      val event = AuthorisationFailureAuditEvent(
        pillar2Id = "XCCVRUGFJG788",
        errorCode = "INVALID_ENROLMENT",
        reason = None,
        user = Some(
          AuthDetails(
            internalId = Some("internalId"),
            groupId = Some("groupId"),
            credId = Some("credId"),
            affinityGroup = Some("Organisation"),
            credentialRole = Some("User"),
            enrolmentPillar2Id = None,
            enrolments = Seq("IR-CT:Activated")
          )
        )
      )
      when(mockAuditConnector.sendExtendedEvent(any[ExtendedDataEvent]())(using any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(AuditResult.Success))

      await(auditService.auditAuthorisationFailure(event)) mustBe AuditResult.Success

      val captor: ArgumentCaptor[ExtendedDataEvent] = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])
      verify(mockAuditConnector).sendExtendedEvent(captor.capture())(using any[HeaderCarrier](), any[ExecutionContext]())
      captor.getValue.auditSource mustBe "pillar2-submission-api"
      captor.getValue.auditType mustBe "Pillar2ApiAuthorisationFailure"
      captor.getValue.detail mustBe Json.parse(
        """{
          |  "pillar2Id": "XCCVRUGFJG788",
          |  "errorCode": "INVALID_ENROLMENT",
          |  "user": {
          |    "internalId": "internalId",
          |    "groupId": "groupId",
          |    "credId": "credId",
          |    "affinityGroup": "Organisation",
          |    "credentialRole": "User",
          |    "enrolments": ["IR-CT:Activated"]
          |  }
          |}""".stripMargin
      )
    }
  }
}
