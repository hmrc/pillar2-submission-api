import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.7.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-30" % bootstrapVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"      % "2.20.0",
    "org.typelevel"                %% "cats-core"                 % "2.13.0",
    "com.beachape"                 %% "enumeratum-play-json"      % "1.9.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30" % bootstrapVersion % Test,
    "org.scalatest"          %% "scalatest"              % "3.2.19"         % Test,
    "com.vladsch.flexmark"    % "flexmark-all"           % "0.64.8"         % Test,
    "org.mockito"             % "mockito-core"           % "5.20.0"         % Test,
    "org.scalatestplus"      %% "mockito-4-11"           % "3.2.18.0"       % Test,
    "org.scalatestplus.play" %% "scalatestplus-play"     % "7.0.2"          % Test,
    "org.scalatestplus"      %% "scalacheck-1-18"        % "3.2.19.0"       % Test,
    "com.networknt"           % "json-schema-validator"  % "1.5.7"          % Test
  )

  val it: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test
  )

}
