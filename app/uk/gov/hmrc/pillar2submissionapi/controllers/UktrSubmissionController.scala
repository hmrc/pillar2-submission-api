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
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions._
import uk.gov.hmrc.pillar2submissionapi.validation._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}

@Singleton
class UktrSubmissionController @Inject() (
  cc:                          ControllerComponents,
  liabilityDataValidator:      LiabilityDataValidator,
  liabilityNilReturnValidator: LiabilityNilReturnValidator
) extends BackendController(cc) {

  def submitUktr: Action[AnyContent] = Action { request =>
    request.body.asJson match {
      case Some(json) =>
        val knownTopLevelKeys = Set("accountingPeriodFrom", "accountingPeriodTo", "obligationMTT", "electionUKGAAP", "liabilities")
        val extraTopLevelKeys = json.as[JsObject].keys.diff(knownTopLevelKeys)

        val knownLiabilitiesKeys = Set(
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
        val extraLiabilitiesKeys = (json \ "liabilities")
          .asOpt[JsObject]
          .map(_.keys.diff(knownLiabilitiesKeys))
          .getOrElse(Set.empty)

        val allExtraKeys = extraTopLevelKeys.map(key => s"Path: /$key, Errors: unexpected field") ++
          extraLiabilitiesKeys.map(key => s"Path: /liabilities/$key, Errors: unexpected field")

        if (allExtraKeys.nonEmpty) {
          BadRequest(
            Json.obj(
              "message" -> "Invalid JSON format",
              "details" -> allExtraKeys
            )
          )
        } else {
          json.validate[UktrSubmission] match {
            case JsSuccess(submission: UktrSubmissionData, _) =>
              liabilityDataValidator.validate(submission.liabilities) match {
                case Validated.Valid(_) =>
                  Created(Json.obj("status" -> "Created"))
                case Validated.Invalid(errors) =>
                  BadRequest(
                    Json.obj(
                      "message" -> "Invalid JSON format",
                      "details" -> errors.toList.map(e => s"${e.field}: ${e.error}")
                    )
                  )
              }

            case JsSuccess(submission: UktrSubmissionNilReturn, _) =>
              liabilityNilReturnValidator.validate(submission.liabilities) match {
                case Validated.Valid(_) =>
                  Created(Json.obj("status" -> "Created"))
                case Validated.Invalid(errors) =>
                  BadRequest(
                    Json.obj(
                      "message" -> "Invalid JSON format",
                      "details" -> errors.toList.map(e => s"${e.field}, Errors: ${e.error}")
                    )
                  )
              }

            case JsError(errors) =>
              val errorDetails = errors.map { case (path, validationErrors) =>
                validationErrors.map(err => s"Path: $path, Errors: ${err.message}")
              }.flatten
              BadRequest(
                Json.obj(
                  "message" -> "Invalid JSON format",
                  "details" -> errorDetails
                )
              )
          }
        }

      case None =>
        BadRequest(Json.obj("message" -> "Invalid JSON format"))
    }
  }
}
