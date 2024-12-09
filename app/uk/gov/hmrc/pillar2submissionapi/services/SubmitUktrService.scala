package uk.gov.hmrc.pillar2submissionapi.services

import com.google.inject.{Inject, Singleton}
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.connectors.Pillar2Connector
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.UktrSubmission
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.responses.{SubmitUktrErrorResponse, SubmitUktrSuccessResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class SubmitUktrService @Inject()(pillar2Connector: Pillar2Connector)(implicit hc: HeaderCarrier) {

  def submitUktr(request: UktrSubmission): Future[SubmitUktrSuccessResponse] =
    pillar2Connector.submitUktr(request).map(convertToResult)


  private def convertToResult(response: HttpResponse): SubmitUktrSuccessResponse =
    response.status match {
      case 201 =>
        response.json.validate[SubmitUktrSuccessResponse] match {
          case JsSuccess(success, _) => success
          case JsError(errors) =>
            throw new RuntimeException(s"Failed to parse success response: $errors")
        }
      case 422 =>
        response.json.validate[SubmitUktrErrorResponse] match {
          case JsSuccess(response, _) =>
            throw new RuntimeException(s"422: ${response.code}, ${response.message}")
          case JsError(_) =>
            throw new RuntimeException(s"500")
        }
      case _ =>
        throw new RuntimeException(s"500")
    }
}
