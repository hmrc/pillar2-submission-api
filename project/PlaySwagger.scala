import com.iheart.sbtPlaySwagger.SwaggerPlugin.autoImport._
import sbt.Def

object PlaySwagger {
  lazy val settings: Seq[Def.Setting[_]] = Seq(
    swaggerDomainNameSpaces := Seq("uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions"),
    swaggerRoutesFile := "app.routes",
    swaggerV3 := true,
    swaggerPrettyJson := true
  )
}
