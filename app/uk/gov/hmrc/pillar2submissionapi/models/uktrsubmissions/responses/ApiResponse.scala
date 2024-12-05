package uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.responses

import play.api.libs.json.{Json, OFormat}

sealed trait ApiResponse

case class SubmitUktrSuccessResponse(processingDate:   String,
                                     formBundleNumber: String,
                                     chargeReference:  Option[String]) extends ApiResponse

case class SubmitUktrErrorResponse(code: String,
                                   message: String) extends ApiResponse

object ApiResponse {
  implicit val errorFormat: OFormat[SubmitUktrErrorResponse] = Json.format[SubmitUktrErrorResponse]
  implicit val successFormat: OFormat[SubmitUktrSuccessResponse] = Json.format[SubmitUktrSuccessResponse]

  val internalServerError: SubmitUktrErrorResponse = SubmitUktrErrorResponse("500", "Internal server error")
}



