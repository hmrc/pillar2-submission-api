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
import play.api.mvc.Result
import play.api.mvc.Results.BadRequest
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.pillar2submissionapi.config.AppConfig
import uk.gov.hmrc.pillar2submissionapi.models.subscription._

import java.net.URI
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionConnector @Inject() (val config: AppConfig, val http: HttpClientV2) extends Logging {

  def readSubscription(plrReference: String)(using hc: HeaderCarrier, ec: ExecutionContext): Future[Either[Result, SubscriptionRead]] =
    if config.readSubscriptionV2Enabled then {
      readSubscriptionDataV2(plrReference)
    } else {
      readSubscriptionData(plrReference)
    }

  private def readSubscriptionData(
    plrReference: String
  )(using hc: HeaderCarrier, ec: ExecutionContext): Future[Either[Result, SubscriptionData]] = {
    val url = s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/subscription/read-subscription/$plrReference"
    http.get(URI.create(url).toURL).execute[HttpResponse].map {
      case httpResponse if httpResponse.status == 200 =>
        httpResponse.json
          .validate[SubscriptionSuccess]
          .fold(
            errors => {
              logger.warn(s"Failed to parse read subscription (V1) response: $errors")
              Left(BadRequest)
            },
            parsedSubscriptionSuccess => Right(parsedSubscriptionSuccess.success)
          )
      case resp =>
        logger.warn(s"Connection issue when calling read subscription (V1) with status: ${resp.status}")
        Left(BadRequest)
    }
  }

  private def readSubscriptionDataV2(
    plrReference: String
  )(using hc: HeaderCarrier, ec: ExecutionContext): Future[Either[Result, SubscriptionDataV2]] = {
    val url = s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/subscription/v2/read-subscription/$plrReference"
    http.get(URI.create(url).toURL).execute[HttpResponse].map {
      case httpResponse if httpResponse.status == 200 =>
        httpResponse.json
          .validate[SubscriptionSuccessV2]
          .fold(
            errors => {
              logger.warn(s"Failed to parse read subscription (V2) response: $errors")
              Left(BadRequest)
            },
            parsedSubscriptionSuccessV2 => Right(parsedSubscriptionSuccessV2.success)
          )
      case resp =>
        logger.warn(s"Connection issue when calling read subscription (V2) with status: ${resp.status}")
        Left(BadRequest)
    }
  }

}
