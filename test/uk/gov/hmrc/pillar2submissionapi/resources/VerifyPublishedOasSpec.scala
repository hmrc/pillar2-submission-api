/*
 * Copyright 2025 HM Revenue & Customs
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
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.File

class VerifyPublishedOasSpec extends AnyWordSpec with Matchers {

  "Prod OAS file to be identical to code-generated (ignoring version)" in {
    val yamlMapper = new ObjectMapper(new YAMLFactory())

    val generatedOas = yamlMapper.readTree(new File("target/swagger/application.yaml"))
    val prodOas      = yamlMapper.readTree(new File("resources/public/api/conf/1.0/application.yaml"))

    generatedOas.get("info").asInstanceOf[ObjectNode].remove("version")
    prodOas.get("info").asInstanceOf[ObjectNode].remove("version")

    val generatedString = yamlMapper.writeValueAsString(generatedOas)
    val prodString      = yamlMapper.writeValueAsString(prodOas)

    generatedString shouldBe prodString
  }
}
