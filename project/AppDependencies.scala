import sbt._

object AppDependencies {

  private val bootstrapVersion = "8.3.0"
  

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "com.github.java-json-tools"    % "json-schema-validator"       % "2.2.14",
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"       % "2.12.2",
    "org.typelevel"                 %% "cats-core"                  % "2.3.1"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % bootstrapVersion            % "test, it",
    "org.scalatest"          %% "scalatest"               % "3.1.1"          % Test,
    "com.vladsch.flexmark"   %  "flexmark-all"            % "0.35.10"        % "test, it",
    "org.mockito"            % "mockito-core"             % "3.7.7"          % "test,it",
    "org.scalatestplus"      %% "mockito-3-4"             % "3.2.7.0"        % "test, it",
    "org.scalatestplus.play" %% "scalatestplus-play"      % "5.1.0"          % "test, it"


  )
}
