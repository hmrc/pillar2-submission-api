import org.typelevel.scalacoptions.ScalacOptions
import play.sbt.PlayImport.PlayKeys.playDefaultPort
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.DefaultBuildSettings.*

ThisBuild / scalaVersion := "3.7.3"
ThisBuild / majorVersion := 0

val scalafixSettings = Seq(
  semanticdbEnabled := true,
  semanticdbVersion := scalafixSemanticdb.revision
)

lazy val microservice = Project("pillar2-submission-api", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin, ScalafixPlugin, SwaggerPlugin)
  .settings(
    majorVersion := 0,
    Compile / scalafmtOnCompile := true,
    Test / scalafmtOnCompile := true,
    Compile / tpolecatExcludeOptions ++= Set(
      ScalacOptions.warnNonUnitStatement,
      ScalacOptions.warnValueDiscard,
      ScalacOptions.warnUnusedImports
    ),
    playDefaultPort := 10054,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    scalafixSettings
  )
  .settings(CodeCoverageSettings.settings *)
  .settings(
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources"
  )
  .settings(scalaSettings *)
  .configs(IntegrationTest)
  .settings(DefaultBuildSettings.itSettings() *)
  .settings(
    Test / unmanagedSourceDirectories := (Test / baseDirectory)(base => Seq(base / "test", base / "test-common")).value,
    Test / unmanagedResourceDirectories := Seq(baseDirectory.value / "test-resources")
  )
  .settings(
    IntegrationTest / unmanagedSourceDirectories :=
      (IntegrationTest / baseDirectory)(base => Seq(base / "it", base / "test-common")).value
  )
  .settings(JsonToYaml.settings *)
  .settings(Validate.settings *)
  .settings(PublishTestOnlyOas.settings *)
  .settings(PlaySwagger.settings *)
  .disablePlugins(JUnitXmlReportPlugin)

addCommandAlias("prePrChecks", ";scalafmtCheckAll;scalafmtSbtCheck;scalafixAll --check")
addCommandAlias("lint", ";scalafmtAll;scalafmtSbt;scalafixAll")
addCommandAlias("createOpenAPISpec", ";clean;routesToYamlOas; validateOas")
addCommandAlias("publishTestOnlyOas", ";createOpenAPISpec; publishOas")

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(
    DefaultBuildSettings.itSettings(),
    libraryDependencies ++= AppDependencies.it
  )
  .settings(
    DefaultBuildSettings.itSettings(),
    tpolecatExcludeOptions ++= Set(
      ScalacOptions.warnNonUnitStatement,
      ScalacOptions.warnValueDiscard,
      ScalacOptions.warnUnusedImports
    )
  )

Compile / scalacOptions ++= Seq(
  "-Wconf:src=routes/.*:s",
  "-Wconf:msg=Flag.*set repeatedly:s",
  "-Wconf:msg=Setting -Wunused set to all redundantly:s"
)
