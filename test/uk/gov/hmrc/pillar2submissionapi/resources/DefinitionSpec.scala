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

package uk.gov.hmrc.pillar2submissionapi.resources

import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.report.LogLevel
import com.github.fge.jsonschema.main.JsonSchemaFactory
import play.twirl.api.TwirlHelperImports.twirlJavaCollectionToScala
import uk.gov.hmrc.pillar2submissionapi.base.UnitTestBaseSpec

import scala.io.Source

class DefinitionSpec extends UnitTestBaseSpec {

  private val schemaUrl = "https://raw.githubusercontent.com/hmrc/api-publisher/main/app/resources/api-definition-schema.json"

  "API Definition" should {
    "conform to the API Definition schema" in {
      val source = Source.fromURL(schemaUrl)
      val schemaJson =
        try source.mkString
        finally source.close()

      val schema     = JsonLoader.fromString(schemaJson)
      val definition = JsonLoader.fromResource("/public/api/definition.json")
      val validator  = JsonSchemaFactory.byDefault().getJsonSchema(schema)

      val report = validator.validate(definition)

      val errors = report
        .filter(_.getLogLevel == LogLevel.ERROR)
        .map(error => s"${error.asJson().get("instance")}: ${error.getMessage}")
        .toList

      errors mustEqual List.empty
    }
  }
}
