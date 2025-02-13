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

package uk.gov.hmrc.pillar2submissionapi.base

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.mvc._
import play.api.test.Helpers.stubControllerComponents
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.test.HttpClientSupport
import uk.gov.hmrc.pillar2submissionapi.connectors.SubmitBTNConnector
import uk.gov.hmrc.pillar2submissionapi.connectors.TestOrganisationConnector
import uk.gov.hmrc.pillar2submissionapi.connectors.UKTaxReturnConnector
import uk.gov.hmrc.pillar2submissionapi.helpers.{UKTaxReturnDataFixture, WireMockServerHandler}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.ExecutionContext

trait UnitTestBaseSpec
    extends AnyWordSpec
    with Results
    with Matchers
    with GuiceOneAppPerSuite
    with MockitoSugar
    with WireMockServerHandler
    with UKTaxReturnDataFixture
    with HttpClientSupport {

  implicit val cc:                ControllerComponents = stubControllerComponents()
  implicit val ec:                ExecutionContext     = ExecutionContext.Implicits.global
  implicit val hc:                HeaderCarrier        = HeaderCarrier()
  implicit lazy val system:       ActorSystem          = ActorSystem()
  implicit lazy val materializer: Materializer         = Materializer(system)

  protected val mockConfiguration:             Configuration             = mock[Configuration]
  protected val mockServicesConfig:            ServicesConfig            = mock[ServicesConfig]
  protected val mockHttpClient:                HttpClientV2              = mock[HttpClientV2]
  protected val mockPillar2Connector:          UKTaxReturnConnector      = mock[UKTaxReturnConnector]
  protected val mockUKTaxReturnConnector:      UKTaxReturnConnector      = mock[UKTaxReturnConnector]
  protected val mockSubmitBTNConnector:        SubmitBTNConnector        = mock[SubmitBTNConnector]
  protected val mockTestOrganisationConnector: TestOrganisationConnector = mock[TestOrganisationConnector]
}
