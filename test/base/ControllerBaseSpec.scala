package base

import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc.{ControllerComponents, Results}
import play.api.test.Helpers.stubControllerComponents

trait ControllerBaseSpec extends PlaySpec with Results with Matchers with MockitoSugar {

  implicit val cc: ControllerComponents = stubControllerComponents()
}
