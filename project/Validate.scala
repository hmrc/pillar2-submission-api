import _root_.io.swagger.v3.parser.OpenAPIV3Parser
import sbt.Keys._
import sbt._

import scala.jdk.CollectionConverters.asScalaBufferConverter

object Validate {
  val validateOas = taskKey[Unit]("Validate OpenAPI specification")
  def settings: Seq[Setting[_]] = Seq(
    validateOas := {
      val openApiFilePath = baseDirectory.value / "target/swagger/application.yaml"
      val parser          = new OpenAPIV3Parser()
      val result          = parser.readLocation(openApiFilePath.toString, null, null)

      if (!result.getMessages.isEmpty) sys.error(s"Validation failed:\n${result.getMessages.asScala.mkString("\n")}")

    }
  )
}
