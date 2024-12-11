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

package uk.gov.hmrc.pillar2submissionapi.controllers.platform

import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.pillar2submissionapi.base.IntegrationSpecBase
import uk.gov.hmrc.play.bootstrap.http.HttpClientV2Provider

import java.net.URI
import scala.annotation.nowarn
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.io.Source

@nowarn
class DocumentationControllerISpec extends IntegrationSpecBase {

  implicit val hc: HeaderCarrier = new HeaderCarrier
  val provider = app.injector.instanceOf[HttpClientV2Provider]
  val client   = provider.get()
  val baseUrl  = s"http://localhost:$port"

  "DocumentationController" should {
    "return definition" in {
      val url                = s"$baseUrl${routes.DocumentationController.definition().url}"
      val definition         = Await.result(client.get(URI.create(url).toURL).execute[HttpResponse], 5.seconds)
      val definitionStr      = definition.body
      val expectedDefinition = Source.fromResource("public/api/definition.json").mkString
      definition.status mustEqual 200
      definitionStr mustEqual expectedDefinition
    }

    "return API documentation" in {
      val url            = s"$baseUrl${routes.DocumentationController.specification("1.0", "application.yaml").url}"
      val apiDoc         = Await.result(client.get(URI.create(url).toURL).execute[HttpResponse], 5.seconds)
      val apiDocStr      = apiDoc.body
      val expectedAPIDoc = Source.fromResource("public/api/conf/1.0/application.yaml").mkString
      apiDoc.status mustEqual 200
      apiDocStr mustEqual expectedAPIDoc
    }
  }

}
