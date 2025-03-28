import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.{YAMLFactory, YAMLGenerator, YAMLMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.iheart.sbtPlaySwagger.SwaggerPlugin.autoImport.swagger
import play.api.libs.functional.syntax.*
import play.api.libs.json.*
import play.api.libs.json.Reads.*
import sbt.*
import sbt.Keys.*

import scala.language.postfixOps

object JsonToYaml {

  val apiContext      = "/organisations/pillar-two"
  val routesToYamlOas = taskKey[Unit]("Generate YAML OpenAPI specification from JSON")

  val updateVersion = (__ \ "info" \ "version").json.update(
    __.read[String]
      .map { obj =>
        JsString(obj.stripSuffix("-SNAPSHOT"))
      }
  )

  def settings: Seq[Setting[_]] = Seq(
    routesToYamlOas := {
      swagger.value

      val yamlFactory = new YAMLFactory()
        .configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false)
        .configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true)
      val jsonMapper = new ObjectMapper().registerModule(DefaultScalaModule)
      val yamlMapper = new YAMLMapper().registerModule(DefaultScalaModule)

      val jsonFile: File = baseDirectory.value / "target/swagger/swagger.json"
      val yamlFile: File = baseDirectory.value / "target/swagger/application.yaml"

      val jsonString = IO.read(jsonFile)
      val paths      = (Json.parse(jsonString) \ "paths").as[JsObject].keys.toList.reverse

      val parsedJson = Json
        .parse(jsonString)

      val copyToNewPaths: Reads[JsObject] = paths
        .map { path =>
          __.read[JsObject]
            .flatMapResult { obj =>
              obj.transform(
                __.read[JsObject]
                  .map(o =>
                    o ++ JsObject(
                      List(s"$apiContext$path" -> (parsedJson \ "paths" \ path).as[JsObject])
                    )
                  )
              )
            }
        }
        .fold(__.read[JsObject].map(identity))((a, b) => (a and b).reduce)

      val removeOldPaths = paths.map(path => (__ \ "paths" \ path).json.prune).fold(__.read[JsObject].map(identity))((a, b) => a andThen b)

      val json = parsedJson
        .transform(updateVersion andThen (__ \ "paths").json.update(copyToNewPaths) andThen removeOldPaths)
        .get

      val jsonNodeTree = jsonMapper.readTree(json.toString)

      val jsonAsYaml = yamlMapper.writeValueAsString(jsonNodeTree)
      IO.write(yamlFile, jsonAsYaml)
    }
  )
}
