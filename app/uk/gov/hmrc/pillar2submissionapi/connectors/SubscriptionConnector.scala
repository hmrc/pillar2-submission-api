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
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http._
import uk.gov.hmrc.pillar2submissionapi.config.AppConfig
import uk.gov.hmrc.pillar2submissionapi.models.subscription.SubscriptionLocalData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionConnector @Inject() (val config: AppConfig, val http: HttpClient) extends Logging {

  def getSubscriptionCache(
    userId:      String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SubscriptionLocalData] =
    http
      .GET[HttpResponse](s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/user-cache/read-subscription/$userId")
      .map {
        case response if response.status == 200 =>
          Json.parse(response.body).as[SubscriptionLocalData]
        case e =>
          logger.warn(s"Connection issue when calling read subscription with status: ${e.status} ${e.body}")
          throw new RuntimeException(s"Error: ${e.status} ${e.body}") //TODO: Replace placeholder error
      }
}