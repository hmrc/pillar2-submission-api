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
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.config.AppConfig
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.UktrSubmission

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Pillar2Connector @Inject() (val config: AppConfig, val http: HttpClient)(implicit ec: ExecutionContext) extends Logging {

  private val uktrSubmissionUrl: String = s"${config.pillar2BaseUrl}/UPDATE_THIS_URL"

  def submitUktr(uktrSubmission: UktrSubmission)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    http.POST[UktrSubmission, HttpResponse](uktrSubmissionUrl, uktrSubmission)
}
