import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.{YAMLFactory, YAMLGenerator}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.iheart.sbtPlaySwagger.SwaggerPlugin.autoImport.swagger
import sbt.Keys._
import sbt._

import scala.collection.mutable
import scala.util.Try

object JsonToYaml {
  val routesToYamlSpec = taskKey[Unit]("Generate YAML OpenAPI specification from JSON")
  def settings: Seq[Setting[_]] = Seq(
    routesToYamlSpec := {
      swagger.value

      val yamlFactory = new YAMLFactory().configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false)
      val jsonMapper  = new ObjectMapper().registerModule(DefaultScalaModule)
      val yamlMapper  = new ObjectMapper(yamlFactory).registerModule(DefaultScalaModule)

      val jsonFile: File = baseDirectory.value / "target/swagger/swagger.json"
      val yamlFile: File = baseDirectory.value / "target/swagger/application.yaml"

      Try {
        val jsonString = IO.read(jsonFile)
        val jsonMap    = jsonMapper.readValue(jsonString, classOf[Map[String, Any]])

        val specification = mutable.LinkedHashMap(
          "openapi"    -> jsonMap("openapi"),
          "info"       -> (jsonMap("info").asInstanceOf[Map[String, Any]] + ("version" -> version.value.stripSuffix("-SNAPSHOT"))),
          "tags"       -> jsonMap("tags"),
          "paths"      -> jsonMap("paths"),
          "components" -> jsonMap("components")
        )

        val yamlString = yamlMapper.writeValueAsString(specification)
        IO.write(yamlFile, yamlString)
      }.map(_ => jsonFile.delete)
        .recover { case ex: Exception => streams.value.log.error(s"Failed to convert JSON to YAML: $ex") }
    }
  )
}
