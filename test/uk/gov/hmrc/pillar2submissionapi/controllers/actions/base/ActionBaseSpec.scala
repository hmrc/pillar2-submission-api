package uk.gov.hmrc.pillar2submissionapi.controllers.actions.base

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Results
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.ExecutionContext

trait ActionBaseSpec extends PlaySpec with MockitoSugar with Results with Matchers {

  implicit lazy val ec:           ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit lazy val system:       ActorSystem      = ActorSystem()
  implicit lazy val materializer: Materializer     = Materializer(system)

  val mockAuthConnector: AuthConnector = mock[AuthConnector]
}
