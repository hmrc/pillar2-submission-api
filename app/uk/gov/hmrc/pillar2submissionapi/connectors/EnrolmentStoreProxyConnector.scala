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

package uk.gov.hmrc.pillar2submissionapi.connectors


import play.api.Logging
import play.api.http.Status.{NO_CONTENT, OK}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.mvc.Results.BadRequest
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.config.AppConfig
import uk.gov.hmrc.pillar2submissionapi.models.enrolments.GroupDelegatedEnrolment

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class EnrolmentStoreProxyConnector @Inject()(implicit ec: ExecutionContext, val config: AppConfig, val http: HttpClient) extends Logging {

  def getDelegatedEnrolment(
                             groupId: String
                           )(implicit hc: HeaderCarrier): Future[Either[Result, GroupDelegatedEnrolment]] = {
    val url = s"${config.enrolmentStoreProxyUrl}/enrolment-store/groups/$groupId/delegated"
    println(url)
    logger.info(s"Getting enrolments for groupId: $groupId")
    http.GET[HttpResponse](url).map {
        case response if response.status == OK =>
          logger.info(s"Delegated enrolment retrieval successful")
          Right(Json.parse(response.body).as[GroupDelegatedEnrolment])
        case response if response.status == NO_CONTENT =>
          logger.info(s"No delegated enrolment found")
          Right(Json.parse(response.body).as[GroupDelegatedEnrolment])
        case e @ _ =>
          logger.error(s"Delegate enrolment error for $groupId - status=${e.status} - error=${e.body}")
          Left(BadRequest)

      }

  }

}
