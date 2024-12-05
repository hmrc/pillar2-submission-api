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

package uk.gov.hmrc.pillar2submissionapi

import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.mvc.{ControllerComponents, Results}
import play.api.test.Helpers.stubControllerComponents
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.config.AppConfig
import uk.gov.hmrc.pillar2submissionapi.connectors.Pillar2Connector
import uk.gov.hmrc.pillar2submissionapi.controllers.actions.IdentifierAction
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.UktrSubmission
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

trait UnitTestBaseSpec extends PlaySpec with Results with Matchers with MockitoSugar {

  implicit val cc: ControllerComponents = stubControllerComponents()
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockConfiguration: Configuration = mock[Configuration]
  val mockServicesConfig: ServicesConfig = mock[ServicesConfig]
  val mockHttpClient:     HttpClientV2   = mock[HttpClientV2]
  val mockIdentifierAction: IdentifierAction = mock[IdentifierAction]
  val mockPillar2Connector: Pillar2Connector = mock[Pillar2Connector]

  when(mockPillar2Connector.submitUktr(ArgumentMatchers.eq(any[UktrSubmission])))
    .thenReturn(Future.successful(HttpResponse.apply(201, "Created")))

  val appConfig: AppConfig = new AppConfig(mockConfiguration, mockServicesConfig) {
    override val pillar2BaseUrl: String = "http://localhost:10051"
  }
}
