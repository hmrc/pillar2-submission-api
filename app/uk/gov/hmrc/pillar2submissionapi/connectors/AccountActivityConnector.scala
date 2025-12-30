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

package uk.gov.hmrc.pillar2submissionapi.connectors

import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.pillar2submissionapi.config.AppConfig

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AccountActivityConnector @Inject() (config: AppConfig, httpClient: HttpClientV2)(using ExecutionContext) {

  def getAccountActivity(activityFromDate: LocalDate, activityToDate: LocalDate)(using HeaderCarrier): Future[HttpResponse] = {
    val url = url"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/account-activity?dateFrom=$activityFromDate&dateTo=$activityToDate"

    httpClient.get(url).execute[HttpResponse]
  }

}
