import play.sbt.PlayImport.PlayKeys.playDefaultPort
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings._

val scalafixSettings = Seq(
  semanticdbEnabled := true, // enable SemanticDB
  semanticdbVersion := scalafixSemanticdb.revision
)

lazy val microservice = Project("pillar2-submission-api", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin, ScalafixPlugin, SwaggerPlugin)
  .settings(
    majorVersion := 0,
    ScoverageKeys.coverageExcludedFiles :=
      "<empty>;com.kenshoo.play.metrics.*;.*definition.*;prod.*;testOnlyDoNotUseInAppConf.*;" +
        "app.*;.*BuildInfo.*;.*Routes.*;.*repositories.*;.*controllers.platform.*;.*controllers.test.*;.*services.test.*;.*metrics.*",
    ScoverageKeys.coverageMinimumStmtTotal := 80,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    Compile / scalafmtOnCompile := true,
    Test / scalafmtOnCompile := true,
    playDefaultPort := 10054,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    // https://www.scala-lang.org/2021/01/12/configuring-and-suppressing-warnings.html
    // suppress warnings in generated routes files
    scalacOptions ++= Seq(
      "-Wconf:src=routes/.*:s",
      "-Ywarn-dead-code",
      "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
      "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
      "-Ywarn-unused:locals", // Warn if a local definition is unused.
      "-Ywarn-unused:params", // Warn if a value parameter is unused.
      "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
      "-Ywarn-unused:privates" // Warn if a private member is unused.
    ),
    scalafixSettings
  )
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(CodeCoverageSettings.settings: _*)
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
  .settings(PlaySwagger.settings *)
  .disablePlugins(JUnitXmlReportPlugin)

addCommandAlias("prePrChecks", ";scalafmtCheckAll;scalafmtSbtCheck;scalafixAll --check")
addCommandAlias("lint", ";scalafmtAll;scalafmtSbt;scalafixAll")

/*lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(libraryDependencies ++= AppDependencies.it)*/
