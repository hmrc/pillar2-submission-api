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

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.test.Helpers.stubControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.pillar2submissionapi.connectors.Pillar2Connector
import uk.gov.hmrc.pillar2submissionapi.controllers.actions.AuthenticatedIdentifierAction
import uk.gov.hmrc.pillar2submissionapi.models.requests.IdentifierRequest
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

trait UnitTestBaseSpec extends AnyWordSpec with Results with Matchers with MockitoSugar with WireMockServerHandler {

  implicit val cc:                ControllerComponents = stubControllerComponents()
  implicit val ec:                ExecutionContext     = ExecutionContext.Implicits.global
  implicit val hc:                HeaderCarrier        = HeaderCarrier()
  implicit lazy val system:       ActorSystem          = ActorSystem()
  implicit lazy val materializer: Materializer         = Materializer(system)

  val mockConfiguration:    Configuration    = mock[Configuration]
  val mockServicesConfig:   ServicesConfig   = mock[ServicesConfig]
  val mockHttpClient:       HttpClient       = mock[HttpClient]
  val mockAuthConnector:    AuthConnector    = mock[AuthConnector]
  val mockPillar2Connector: Pillar2Connector = mock[Pillar2Connector]

  val stubIdentifierAction: AuthenticatedIdentifierAction = new AuthenticatedIdentifierAction(
    mockAuthConnector,
    new BodyParsers.Default
  ) {
    override def refine[A](request: Request[A]): Future[Either[Result, IdentifierRequest[A]]] =
      Future.successful(Right(IdentifierRequest(request, "internalId", Some("groupID"), userIdForEnrolment = "userId", clientPillar2Id = "")))
  }

//  val appConfig: AppConfig = new AppConfig(mockConfiguration, mockServicesConfig) {
//    override val pillar2BaseUrl: String = "http://localhost:10051"
//  }

  protected def stubResponse(
    expectedUrl:    String,
    expectedStatus: Int,
    body:           JsValue
  ): StubMapping =
    server.stubFor(
      post(urlEqualTo(expectedUrl))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
            .withBody(body.toString())
        )
    )
}
