package uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.responses

import play.api.libs.json.{Json, OFormat}

case class SubmitUktrErrorResponse(code: String, message: String)

case object SubmitUktrErrorResponse {
  implicit val errorFormat:   OFormat[SubmitUktrErrorResponse]   = Json.format[SubmitUktrErrorResponse]
  val internalServerError: SubmitUktrErrorResponse = SubmitUktrErrorResponse("500", "Internal server error")
}