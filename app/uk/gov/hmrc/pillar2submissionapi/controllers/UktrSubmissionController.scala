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

package uk.gov.hmrc.pillar2submissionapi.controllers

import cats.data.Validated
import cats.implicits.toFoldableOps
import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.pillar2submissionapi.errorhandling.ResponseHandler
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions._
import uk.gov.hmrc.pillar2submissionapi.validation._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}

@Singleton
class UktrSubmissionController @Inject() (
  cc:                          ControllerComponents,
  liabilityDataValidator:      LiabilityDataValidator,
  liabilityNilReturnValidator: LiabilityNilReturnValidator,
  responseHandler:             ResponseHandler
) extends BackendController(cc) {

  private lazy val knownTopLevelKeys = Set(
    "accountingPeriodFrom",
    "accountingPeriodTo",
    "obligationMTT",
    "electionUKGAAP",
    "liabilities"
  )

  private lazy val knownLiabilitiesKeys = Set(
    "electionDTTSingleMember",
    "electionUTPRSingleMember",
    "numberSubGroupDTT",
    "numberSubGroupUTPR",
    "totalLiability",
    "totalLiabilityDTT",
    "totalLiabilityIIR",
    "totalLiabilityUTPR",
    "liableEntities",
    "returnType"
  )

  def submitUktr: Action[AnyContent] = Action { request =>
    request.body.asJson
      .map(validateJson)
      .getOrElse(responseHandler.badRequest("Invalid JSON format", Some(Seq("Body must be valid JSON"))))
  }

  private def validateJson(json: JsValue): Result = {
    val extraKeys = findExtraKeys(json)
    if (extraKeys.nonEmpty) {
      responseHandler.badRequest(
        "Invalid JSON format",
        Some(extraKeys.map(key => s"Path: $key, Errors: unexpected field"))
      )
    } else {
      processSubmission(json)
    }
  }

  private def findExtraKeys(json: JsValue): Seq[String] = {
    val topLevelExtra = json.as[JsObject].keys.diff(knownTopLevelKeys).toSeq
    val liabilitiesExtra = (json \ "liabilities")
      .asOpt[JsObject]
      .map(_.keys.diff(knownLiabilitiesKeys).toSeq)
      .getOrElse(Seq.empty)

    topLevelExtra.map(key => s"/$key") ++ liabilitiesExtra.map(key => s"/liabilities/$key")
  }

  private def processSubmission(json: JsValue): Result =
    json.validate[UktrSubmission] match {
      case JsSuccess(data: UktrSubmissionData, _) =>
        handleValidation(liabilityDataValidator.validate(data.liabilities).leftMap(_.toList))
      case JsSuccess(nilReturn: UktrSubmissionNilReturn, _) =>
        handleValidation(liabilityNilReturnValidator.validate(nilReturn.liabilities).leftMap(_.toList))
      case JsError(errors) =>
        val immutableErrors: scala.collection.immutable.Seq[(JsPath, scala.collection.immutable.Seq[JsonValidationError])] =
          errors.map { case (path, validationErrors) => (path, validationErrors.toVector) }.toSeq
        responseHandler.badRequest("Invalid JSON format", Some(formatJsonValidationErrors(immutableErrors)))
    }

  private def handleValidation(validationResult: Validated[Seq[ValidationError], _]): Result =
    validationResult match {
      case Validated.Valid(_) =>
        responseHandler.created()
      case Validated.Invalid(errors) =>
        responseHandler.badRequest("Validation failed", Some(formatValidationErrors(errors)))
    }

  private def formatJsonValidationErrors(
    errors: Seq[(JsPath, scala.collection.immutable.Seq[JsonValidationError])]
  ): Seq[String] =
    errors.flatMap { case (path, validationErrors) =>
      validationErrors.map(err => s"Path: $path, Errors: ${err.message}")
    }

  private def formatValidationErrors(validationErrors: Seq[ValidationError]): Seq[String] =
    validationErrors.map(err => s"${err.field}: ${err.error}")
}
