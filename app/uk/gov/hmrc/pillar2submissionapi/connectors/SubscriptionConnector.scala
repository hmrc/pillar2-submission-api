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
import play.api.mvc.Result
import play.api.mvc.Results.BadRequest
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.pillar2submissionapi.config.AppConfig
import uk.gov.hmrc.pillar2submissionapi.models.subscription.{SubscriptionData, SubscriptionSuccess}

import java.net.URI
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionConnector @Inject() (val config: AppConfig, val http: HttpClientV2) extends Logging {

  def readSubscription(
    plrReference: String
  )(implicit hc:  HeaderCarrier, ec: ExecutionContext): Future[Either[Result, SubscriptionData]] = {
    val subscriptionUrl = s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/subscription/read-subscription/$plrReference"

    val request = http
      .get(URI.create(subscriptionUrl).toURL())

    request.execute[HttpResponse].map {
      case response if response.status == 200 =>
        Right(Json.parse(response.body).as[SubscriptionSuccess].success)
      case e =>
        logger.warn(s"Connection issue when calling read subscription with status: ${e.status}")
        Left(BadRequest)
    }
  }
}
