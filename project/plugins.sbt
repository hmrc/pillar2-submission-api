resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
resolvers += Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)
resolvers += Resolver.typesafeRepo("releases")
libraryDependencies ++= Seq(
  "com.fasterxml.jackson.module"    %% "jackson-module-scala"    % "2.18.2",
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % "2.17.2",
  "io.swagger.parser.v3"             % "swagger-parser"          % "2.1.23"
)

addSbtPlugin("uk.gov.hmrc"            % "sbt-auto-build"     % "3.24.0")
addSbtPlugin("uk.gov.hmrc"            % "sbt-distributables" % "2.5.0")
addSbtPlugin("org.playframework"      % "sbt-plugin"         % "3.0.6")
addSbtPlugin("org.scoverage"          % "sbt-scoverage"      % "2.2.2")
addSbtPlugin("org.scalameta"          % "sbt-scalafmt"       % "2.5.2")
addSbtPlugin("ch.epfl.scala"          % "sbt-scalafix"       % "0.13.0")
addSbtPlugin("io.github.play-swagger" % "sbt-play-swagger"   % "2.0.4")
addSbtPlugin("org.typelevel"          % "sbt-tpolecat"       % "0.5.2")
