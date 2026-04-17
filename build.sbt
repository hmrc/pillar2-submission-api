import org.typelevel.scalacoptions.ScalacOptions
import play.sbt.PlayImport.PlayKeys.playDefaultPort
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.DefaultBuildSettings.*

ThisBuild / scalaVersion := "3.3.6"
ThisBuild / majorVersion := 0
ThisBuild / scalacOptions ++= Seq(
  "-Wconf:src=routes/.*:s",
  "-Wconf:msg=Flag.*set repeatedly:s",
  "-Wconf:msg=Setting -Wunused set to all redundantly:s"
)

lazy val microservice: Project = Project("pillar2-submission-api", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin, ScalafixPlugin, SwaggerPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    playDefaultPort := 10054,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    semanticdbEnabled := true,
    Compile / scalafmtOnCompile := true,
    Test / scalafmtOnCompile := true,
    Compile / tpolecatExcludeOptions ++= Set(
      ScalacOptions.warnNonUnitStatement,
      ScalacOptions.warnValueDiscard,
      ScalacOptions.warnUnusedImports
    ),
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
    Test / unmanagedResourceDirectories := Seq(baseDirectory.value / "test-resources"),
    Test / unmanagedSourceDirectories := (Test / baseDirectory)(base => Seq(base / "test")).value
  )
  .settings(scalaSettings *)
  .settings(CodeCoverageSettings.settings *)
  .settings(JsonToYaml.settings *)
  .settings(Validate.settings *)
  .settings(PublishTestOnlyOas.settings *)
  .settings(PlaySwagger.settings *)

lazy val it: Project = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(
    DefaultBuildSettings.itSettings(),
    libraryDependencies ++= AppDependencies.it,
    Test / unmanagedSourceDirectories := Seq((Test / baseDirectory).value / "test"),
    Test / unmanagedResourceDirectories := Seq((microservice / baseDirectory).value / "test-resources"),
    Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/test-reports/html-it-report"),
    tpolecatExcludeOptions ++= Set(
      ScalacOptions.warnNonUnitStatement,
      ScalacOptions.warnValueDiscard,
      ScalacOptions.warnUnusedImports
    )
  )

addCommandAlias("prePrChecks", "; scalafmtCheckAll; scalafmtSbtCheck; scalafixAll --check")
addCommandAlias("lint", "; scalafmtAll; scalafmtSbt; scalafixAll")
addCommandAlias("createOpenAPISpec", "; clean; routesToYamlOas; validateOas")
addCommandAlias("publishTestOnlyOas", "; createOpenAPISpec; publishOas")
