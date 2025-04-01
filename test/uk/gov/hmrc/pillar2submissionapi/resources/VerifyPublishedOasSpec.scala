package uk.gov.hmrc.pillar2submissionapi.resources

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.File
import scala.sys.process._

class VerifyPublishedOasSpec extends AnyWordSpec with Matchers with BeforeAndAfterAll {

  override def beforeAll(): Unit = {
    super.beforeAll()
    "sbt createOpenAPISpec".!
  }
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
