import com.iheart.sbtPlaySwagger.SwaggerPlugin.autoImport._
import sbt.Def

object PlaySwagger {
  lazy val settings: Seq[Def.Setting[_]] = Seq(
    swaggerDomainNameSpaces := Seq("app/models"),
    swaggerRoutesFile := "app.routes",
    swaggerV3 := true,
    swaggerPrettyJson := true
  )
}
