package base

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.{Materializer, SystemMaterializer}
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc.{ControllerComponents, Results}
import play.api.test.Helpers.stubControllerComponents

trait ControllerBaseSpec extends PlaySpec
  with Results
  with Matchers
  with MockitoSugar
  with TableDrivenPropertyChecks {

  implicit val cc: ControllerComponents = stubControllerComponents()
  implicit val materializer: Materializer = SystemMaterializer.get(ActorSystem()).materializer
}
