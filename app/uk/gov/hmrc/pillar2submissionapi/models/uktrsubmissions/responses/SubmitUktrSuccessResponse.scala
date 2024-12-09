package uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions.responses

import play.api.libs.json.{Json, OFormat}

case class SubmitUktrSuccessResponse(processingDate: String, formBundleNumber: String, chargeReference: Option[String])

case object SubmitUktrSuccessResponse {
  implicit val successFormat: OFormat[SubmitUktrSuccessResponse] = Json.format[SubmitUktrSuccessResponse]
}