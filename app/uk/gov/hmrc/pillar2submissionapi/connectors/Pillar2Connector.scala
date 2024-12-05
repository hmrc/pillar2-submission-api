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
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.config.AppConfig
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.UktrSubmission
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.UktrSubmission.uktrSubmissionReads

import java.net.URL
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Pillar2Connector @Inject()(val config: AppConfig, val http: HttpClientV2)(implicit ec: ExecutionContext) extends Logging {

  private val uktrSubmissionUrl: String = s"${config.pillar2BaseUrl}/UPDATE_THIS_URL"

  def submitUktr(uktrSubmission: UktrSubmission)(implicit hc: HeaderCarrier): Future[HttpResponse] = {

    http
      .post(new URL(uktrSubmissionUrl))
      .withBody(Json.toJson(uktrSubmission))
      .execute[HttpResponse]
  }
}
