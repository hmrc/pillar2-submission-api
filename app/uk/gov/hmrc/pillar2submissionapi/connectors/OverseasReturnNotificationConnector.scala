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
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.config.AppConfig
import uk.gov.hmrc.pillar2submissionapi.models.overseasreturnnotification.ORNSubmission

import java.net.URI
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OverseasReturnNotificationConnector @Inject() (val config: AppConfig, val http: HttpClientV2)(implicit ec: ExecutionContext) extends Logging {

  private val ORNSubmitUrl: String = s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/overseas-return-notification/submit"
  private val ORNAmendUrl:  String = s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/overseas-return-notification/amend"
  private def ORNRetrieveUrl(accountingPeriodFrom: String, accountingPeriodTo: String): String =
    s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/overseas-return-notification/$accountingPeriodFrom/$accountingPeriodTo"

  def submitORN(ORNSubmission: ORNSubmission)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    logger.info(s"Calling $ORNSubmitUrl to submit a ORN")
    http
      .post(URI.create(ORNSubmitUrl).toURL)
      .withBody(Json.toJson(ORNSubmission))
      .execute[HttpResponse]
  }

  def amendORN(ORNSubmission: ORNSubmission)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    logger.info(s"Calling $ORNAmendUrl to amend a ORN")
    http
      .put(URI.create(ORNAmendUrl).toURL)
      .withBody(Json.toJson(ORNSubmission))
      .execute[HttpResponse]
  }

  def retrieveORN(accountingPeriodFrom: String, accountingPeriodTo: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val url = ORNRetrieveUrl(accountingPeriodFrom, accountingPeriodTo)
    logger.info(s"Calling $url to retrieve a ORN")
    http
      .get(URI.create(url).toURL)
      .execute[HttpResponse]
  }
}
