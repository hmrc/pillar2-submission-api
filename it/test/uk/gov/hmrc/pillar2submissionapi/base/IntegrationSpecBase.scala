/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.pillar2submissionapi.base

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

trait IntegrationSpecBase extends AnyWordSpec
  with BeforeAndAfterEach
  with Matchers
  with Results {

  implicit lazy val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit lazy val hc: HeaderCarrier = HeaderCarrier()
  implicit lazy val system: ActorSystem = ActorSystem()
  implicit lazy val materializer: Materializer = Materializer(system)

  protected def applicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        Configuration.from(
          Map(
            "metrics.enabled" -> "false",
            "auditing.enabled" -> false,
            "features.grsStubEnabled" -> true
          )
        )
      )
}
