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

import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsObject
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.pillar2submissionapi.base.IntegrationSpecBase
import uk.gov.hmrc.play.bootstrap.http.HttpClientV2Provider

import java.net.URI
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.io.Source

class DocumentationControllerISpec extends IntegrationSpecBase {

  val provider = app.injector.instanceOf[HttpClientV2Provider]
  val client   = provider.get()
  val baseUrl  = s"http://localhost:$port"

  "DocumentationController" should {
    "return definition" in {
      val url        = s"$baseUrl${routes.DocumentationController.definition().url}"
      val definition = Await.result(client.get(URI.create(url).toURL).execute[HttpResponse], 5.seconds)
      val json       = definition.json
      (json \ "api" \ "name").as[String] mustEqual "Pillar 2"
      (json \ "api" \ "description").as[String] mustEqual "An API for managing and retrieving Pillar 2 data"
      (json \ "api" \ "context").as[String] mustEqual "organisations/pillar-two"
      (json \ "api" \ "categories").as[List[String]] must have size 1
      (json \ "api" \ "categories").as[List[String]].head mustEqual "CORPORATION_TAX"
      (json \ "api" \ "versions").as[List[JsObject]] must have size 1
      (json \ "api" \ "versions" \ 0 \ "version").as[String] mustEqual "1.0"
      (json \ "api" \ "versions" \ 0 \ "status").as[String] mustEqual "BETA"
      (json \ "api" \ "versions" \ 0 \ "endpointsEnabled").as[Boolean] mustEqual true
      (json \ "api" \ "versions" \ 0 \ "access" \ "type").as[String] mustEqual "PRIVATE"
      (json \ "api" \ "versions" \ 0 \ "access" \ "isTrial").as[Boolean] mustEqual true
    }

    "return API documentation" when {
      "testOnlyOasEnabled is false" in {
        val url            = s"$baseUrl${routes.DocumentationController.specification("1.0", "application.yaml").url}"
        val apiDoc         = Await.result(client.get(URI.create(url).toURL).execute[HttpResponse], 5.seconds)
        val apiDocStr      = apiDoc.body
        val expectedAPIDoc = Source.fromResource("public/api/conf/1.0/application.yaml").mkString

        apiDoc.status mustEqual 200
        apiDocStr mustEqual expectedAPIDoc
      }

      "testOnlyOasEnabled is true" in {
        val app = new GuiceApplicationBuilder()
          .configure("features.testOnlyOasEnabled" -> true)
          .build()

        val request = FakeRequest(GET, routes.DocumentationController.specification("1.0", "application.yaml").url)
        val result  = route(app, request).get

        val apiDocStr      = contentAsString(result)
        val expectedAPIDoc = Source.fromResource("public/api/conf/1.0/testOnly/application.yaml").mkString

        status(result) mustEqual 200
        apiDocStr mustEqual expectedAPIDoc
      }
    }
  }
}
