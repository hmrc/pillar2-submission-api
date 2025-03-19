import sbt.Keys._
import sbt._

object PublishTestOnlyOas {
  val publishOas = taskKey[Unit]("Copy OpenAPI specification to resources directory")

  def settings: Seq[Setting[_]] = Seq(
    publishOas := {
      val sourceFile = baseDirectory.value / "target/swagger/application.yaml"
      val targetDir  = baseDirectory.value / "resources/public/api/conf/testOnly"
      val targetFile = targetDir / "application.yaml"

      if (!sourceFile.exists) {
        sys.error(s"Source file not found: ${sourceFile.getAbsolutePath}")
      }

      IO.createDirectory(targetDir)
      IO.copyFile(sourceFile, targetFile)
      println(s"Successfully copied OpenAPI spec to: ${targetFile.getAbsolutePath}")
    }
  )
}
