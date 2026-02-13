import _root_.io.swagger.v3.parser.OpenAPIV3Parser
import sbt.Keys.*
import sbt.*

import scala.jdk.CollectionConverters.asScalaBufferConverter

object Validate {
  val validateOas = taskKey[Unit]("Validate OpenAPI specification")
  def settings: Seq[Setting[?]] = Seq(
    validateOas := {
      val openApiFilePath = baseDirectory.value / "target/swagger/application.yaml"
      val parser          = new OpenAPIV3Parser()
      val result          = parser.readLocation(openApiFilePath.toString, null, null)

      // scalafmt: { rewrite.scala3.newSyntax.control = false }
      if (!result.getMessages.isEmpty) sys.error(s"Validation failed:\n${result.getMessages.asScala.mkString("\n")}")

    }
  )
}
