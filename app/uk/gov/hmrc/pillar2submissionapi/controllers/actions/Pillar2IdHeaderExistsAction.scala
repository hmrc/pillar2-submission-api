/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.pillar2submissionapi.controllers.actions

import com.google.inject.Inject
import play.api.mvc.{BodyParsers, Request}
import uk.gov.hmrc.pillar2submissionapi.controllers.error.MissingHeader.MissingPillar2Id

import scala.concurrent.{ExecutionContext, Future}

case class Pillar2IdHeaderExistsAction @Inject() (
  parser:                        BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends Pillar2IdHeaderAction {

  override protected def transform[A](request: Request[A]): Future[RequestWithPillar2Id[A]] =
    request.headers
      .get("X-Pillar2-Id")
      .fold(
        Future.failed[RequestWithPillar2Id[A]](
          MissingPillar2Id
        )
      )(pillar2Id => Future.successful(RequestWithPillar2Id(pillar2Id = pillar2Id, request = request)))

}
