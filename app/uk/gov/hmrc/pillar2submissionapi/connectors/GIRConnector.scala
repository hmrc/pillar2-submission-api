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

import play.api.Logging
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.config.AppConfig
import uk.gov.hmrc.pillar2submissionapi.models.globeinformationreturn.GIRSubmission

import java.net.URI
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GIRConnector @Inject() (
  val config: AppConfig,
  val http:   HttpClientV2
)(using ec: ExecutionContext)
    extends Logging {

  def createGIR(request: GIRSubmission)(using hc: HeaderCarrier): Future[HttpResponse] = {
    val url = s"${config.stubBaseUrl}/pillar2/test/globe-information-return"
    logger.info(s"Calling $url to create GIR submission")

    http
      .post(URI.create(url).toURL)
      .withBody(Json.toJson(request))
      .execute[HttpResponse]
  }
}
