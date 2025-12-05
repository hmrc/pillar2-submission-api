import sbt.Keys.*
import sbt.*

object PublishTestOnlyOas {
  val publishOas = taskKey[Unit]("Copy OpenAPI specification to resources directory")

  def settings: Seq[Setting[?]] = Seq(
    publishOas := {
      val sourceFile = baseDirectory.value / "target/swagger/application.yaml"
      val targetDir  = baseDirectory.value / "resources/public/api/conf/1.0/testOnly"
      val targetFile = targetDir / "application.yaml"

      // scalafmt: { rewrite.scala3.newSyntax.control = false }
      if (!sourceFile.exists) {
        sys.error(s"Source file not found: ${sourceFile.getAbsolutePath}")
      }

      IO.createDirectory(targetDir)
      IO.copyFile(sourceFile, targetFile)
      println(s"Successfully copied OpenAPI spec to: ${targetFile.getAbsolutePath}")
    }
  )
}
