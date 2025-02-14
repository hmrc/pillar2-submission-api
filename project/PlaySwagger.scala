import com.iheart.sbtPlaySwagger.SwaggerPlugin.autoImport._
import sbt.Def

object PlaySwagger {
  lazy val settings: Seq[Def.Setting[_]] = Seq(
    swaggerDomainNameSpaces := Seq(
      "uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions",
      "uk.gov.hmrc.pillar2submissionapi.models.belowthresholdnotification",
      "uk.gov.hmrc.pillar2submissionapi.models.response.Pillar2ErrorResponse",
      "uk.gov.hmrc.pillar2submissionapi.models.response.StubErrorResponse",
      "uk.gov.hmrc.pillar2submissionapi.models.organisation"
    ),
    swaggerRoutesFile := "public.routes",
    swaggerV3 := true,
    swaggerPrettyJson := true
  )
}
