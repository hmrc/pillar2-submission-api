import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.18.0"

  val compile = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-30" % bootstrapVersion,
    "com.github.java-json-tools"    % "json-schema-validator"     % "2.2.14",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"      % "2.20.0",
    "org.typelevel"                %% "cats-core"                 % "2.13.0",
    "com.beachape"                 %% "enumeratum-play-json"      % "1.9.0"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30" % bootstrapVersion % "test, it",
    "org.scalatest"          %% "scalatest"              % "3.2.19"         % Test,
    "com.vladsch.flexmark"    % "flexmark-all"           % "0.64.8"         % "test, it",
    "org.mockito"             % "mockito-core"           % "5.20.0"         % "test,it",
    "org.scalatestplus"      %% "mockito-4-11"           % "3.2.18.0"       % "test, it",
    "org.scalatestplus.play" %% "scalatestplus-play"     % "7.0.2"          % "test, it",
    "org.scalatestplus"      %% "scalacheck-1-18"        % "3.2.19.0"       % "test"
  )

  val it: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test
  )
}
