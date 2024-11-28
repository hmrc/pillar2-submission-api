/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.pillar2submissionapi.helpers

import org.apache.pekko.actor.ActorSystem
import org.mockito.Mockito
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.pillar2submissionapi.config.AppConfig
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.annotation.nowarn

trait AllMocks extends MockitoSugar {
  me: BeforeAndAfterEach =>

  val mockActorSystem:    ActorSystem    = mock[ActorSystem]
  val mockAuditConnector: AuditConnector = mock[AuditConnector]
  val mockAuthConnector:  AuthConnector  = mock[AuthConnector]
  val mockAppConfig:      AppConfig      = mock[AppConfig]

  val mockHttpClient: HttpClient = mock[HttpClient]

  val mockAuthorisedFunctions: AuthorisedFunctions = mock[AuthorisedFunctions]

  @nowarn
  override protected def beforeEach(): Unit =
    Seq(
      mockActorSystem,
      mockAuditConnector,
      mockAuthConnector,
      mockAppConfig,
      mockHttpClient,
      mockAuthorisedFunctions
    ).foreach(Mockito.reset(_))
}
