import org.typelevel.scalacoptions.ScalacOptions
import play.sbt.PlayImport.PlayKeys.playDefaultPort
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.DefaultBuildSettings.*

ThisBuild / scalaVersion := "2.13.12"
ThisBuild / majorVersion := 0

val scalafixSettings = Seq(
  semanticdbEnabled := true, // enable SemanticDB
  semanticdbVersion := scalafixSemanticdb.revision
)

lazy val microservice = Project("pillar2-submission-api", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin, ScalafixPlugin, SwaggerPlugin)
  .settings(
    majorVersion := 0,
    Compile / scalafmtOnCompile := true,
    Test / scalafmtOnCompile := true,
    Compile / tpolecatExcludeOptions ++= Set(ScalacOptions.warnNonUnitStatement, ScalacOptions.warnValueDiscard),
    playDefaultPort := 10054,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalafixSettings
  )
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(CodeCoverageSettings.settings *)
  .settings(
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources"
  )
  .settings(scalaSettings: _*)
  .settings(scalaVersion := "2.13.12")
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(
    unmanagedResourceDirectories in Compile += baseDirectory.value / "resources",
    unmanagedSourceDirectories in Test := (baseDirectory in Test)(base => Seq(base / "test", base / "test-common")).value,
    unmanagedResourceDirectories in Test := Seq(baseDirectory.value / "test-resources")
  )
  .settings(
    unmanagedSourceDirectories in IntegrationTest :=
      (baseDirectory in IntegrationTest)(base => Seq(base / "it", base / "test-common")).value,
    testOptions in IntegrationTest += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/test-reports/html-it-report"),
    unmanagedResourceDirectories in IntegrationTest := Seq(baseDirectory.value / "test-resources")
  )
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(resolvers += "emueller-bintray" at "https://dl.bintray.com/emueller/maven")
  .settings(JsonToYaml.settings *)
  .settings(Validate.settings *)
  .settings(PlaySwagger.settings *)
  .disablePlugins(JUnitXmlReportPlugin)

addCommandAlias("prePrChecks", ";scalafmtCheckAll;scalafmtSbtCheck;scalafixAll --check")
addCommandAlias("lint", ";scalafmtAll;scalafmtSbt;scalafixAll")

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(tpolecatExcludeOptions += ScalacOptions.warnNonUnitStatement)
  .settings(libraryDependencies ++= AppDependencies.it)
