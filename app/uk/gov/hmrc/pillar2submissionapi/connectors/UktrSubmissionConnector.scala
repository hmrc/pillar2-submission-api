package uk.gov.hmrc.pillar2submissionapi.connectors

import play.api.Logging
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads.is2xx
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.pillar2submissionapi.config.AppConfig
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.UktrSubmission

import java.net.URL
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UktrSubmissionConnector @Inject() (val config: AppConfig, val http: HttpClientV2)
                                        (implicit ec: ExecutionContext) extends Logging {

  private val uktrSubmissionUrl: String = s"${config.pillar2BaseUrl}/UPDATE_THIS_URL"

  def submitUktr(uktrSubmission: UktrSubmission)(implicit hc: HeaderCarrier): Future[String] = {

    // TODO - Assuming no headers to add in here as they'll be added in pillar2 before we call ETMP


    http.post(new URL(uktrSubmissionUrl))
      .withBody(Json.toJson(uktrSubmission))
      .execute[String] {
        case response if is2xx(response._3.status) =>
          logger.info(s" UPE register without ID successful with response ${response._3}")
          response._3.json.as[String]
        case errorResponse =>
          logger.warn(s"UPE register without ID call failed with status ${errorResponse._3}")
          "oh dear, this has gone wrong"

      }
    Future.successful("")
  }

}
