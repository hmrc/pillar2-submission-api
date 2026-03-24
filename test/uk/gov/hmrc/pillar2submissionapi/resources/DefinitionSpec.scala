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

import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.{JsonSchemaFactory, SpecVersion}
import uk.gov.hmrc.pillar2submissionapi.base.UnitTestBaseSpec

import scala.io.Source
import scala.jdk.CollectionConverters._

class DefinitionSpec extends UnitTestBaseSpec {

  private val schemaUrl = "https://raw.githubusercontent.com/hmrc/api-publisher/main/app/resources/api-definition-schema.json"

  "API Definition" should {
    "conform to api-publisher schema" in {
      val mapper     = new ObjectMapper()
      val source     = Source.fromURL(schemaUrl)
      val schemaJson =
        try source.mkString
        finally source.close()
      val schemaNode     = mapper.readTree(schemaJson)
      val definitionNode = mapper.readTree(getClass.getResourceAsStream("/public/api/definition.json"))
      val factory        = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)
      val schema         = factory.getSchema(schemaNode)

      val errors = schema.validate(definitionNode).asScala.map(_.getMessage).toList

      errors mustEqual List.empty
    }
  }
}
