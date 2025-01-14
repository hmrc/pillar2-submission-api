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
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.UKTRSubmission

import java.net.URI
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UKTaxReturnConnector @Inject() (val config: AppConfig, val http: HttpClientV2)(implicit ec: ExecutionContext) extends Logging {

  private val uktrSubmissionUrl: String = s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/submit-uk-tax-return"
  private val uktrAmendmentUrl:  String = s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/amend-uk-tax-return"

  def submitUKTR(uktrSubmission: UKTRSubmission)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    logger.info(s"Calling pillar2 backend: $uktrSubmissionUrl")
    val request = http.post(URI.create(uktrSubmissionUrl).toURL()).withBody(Json.toJson(uktrSubmission))
    request.execute[HttpResponse]
  }

  def amendUKTR(uktrSubmission: UKTRSubmission)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    logger.info(s"Calling pillar2 backend: $uktrAmendmentUrl")
    val request = http.put(URI.create(uktrAmendmentUrl).toURL()).withBody(Json.toJson(uktrSubmission))
    request.execute[HttpResponse]
  }
}
