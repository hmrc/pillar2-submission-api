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

package uk.gov.hmrc.pillar2submissionapi.controllers.submission

import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.controllers.actions.{IdentifierAction, SubscriptionDataRetrievalAction}
import uk.gov.hmrc.pillar2submissionapi.controllers.error._
import uk.gov.hmrc.pillar2submissionapi.models.uktrsubmissions._
import uk.gov.hmrc.pillar2submissionapi.services.UKTaxReturnService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UKTaxReturnController @Inject() (
  cc:                       ControllerComponents,
  identify:                 IdentifierAction,
  verifySubscriptionExists: SubscriptionDataRetrievalAction,
  ukTaxReturnService:       UKTaxReturnService
)(implicit ec:              ExecutionContext)
    extends BackendController(cc) {

  def submitUKTR: Action[AnyContent] = (identify andThen verifySubscriptionExists).async { request =>
    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter
        .fromRequest(request)
        .withExtraHeaders("X-Pillar2-Id" -> request.clientPillar2Id)
    request.body.asJson match {
      case Some(request) =>
        request.validate[UKTRSubmission] match {
          case JsSuccess(value, _) =>
            validateMonetaryFields(value) match {
              case Some(error) => Future.failed(error)
              case None =>
                ukTaxReturnService
                  .submitUKTR(value)
                  .map(response => Created(Json.toJson(response)))
            }
          case JsError(_) => Future.failed(InvalidJson)
        }
      case None => Future.failed(EmptyRequestBody)
    }
  }

  def amendUKTR: Action[AnyContent] = (identify andThen verifySubscriptionExists).async { request =>
    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter
        .fromRequest(request)
        .withExtraHeaders("X-Pillar2-Id" -> request.clientPillar2Id)
    request.body.asJson match {
      case Some(request) =>
        request.validate[UKTRSubmission] match {
          case JsSuccess(value, _) =>
            validateMonetaryFields(value) match {
              case Some(error) => Future.failed(error)
              case None =>
                ukTaxReturnService
                  .amendUKTR(value)
                  .map(response => Ok(Json.toJson(response)))
            }
          case JsError(_) => Future.failed(InvalidJson)
        }
      case None => Future.failed(EmptyRequestBody)
    }
  }

  private def validateMonetaryFields(submission: UKTRSubmission): Option[Throwable] = {
    val minValue         = BigDecimal("0")
    val maxValue         = BigDecimal("9999999999999.99")
    val decimalPrecision = 2

    def validateMonetaryValue(value: BigDecimal): Option[Throwable] =
      if (value < minValue || value > maxValue || value.scale > decimalPrecision) {
        Some(MonetaryValidationError)
      } else {
        None
      }

    submission match {
      case data: UKTRSubmissionData =>
        val liabilities = data.liabilities.asInstanceOf[LiabilityData]
        validateMonetaryValue(liabilities.totalLiability)
          .orElse(validateMonetaryValue(liabilities.totalLiabilityDTT))
          .orElse(validateMonetaryValue(liabilities.totalLiabilityIIR))
          .orElse(validateMonetaryValue(liabilities.totalLiabilityUTPR))
          .orElse {

            liabilities.liableEntities.foldLeft[Option[Throwable]](None) { (acc, entity) =>
              acc
                .orElse(validateMonetaryValue(entity.amountOwedDTT))
                .orElse(validateMonetaryValue(entity.amountOwedIIR))
                .orElse(validateMonetaryValue(entity.amountOwedUTPR))
            }
          }

      case _: UKTRSubmissionNilReturn =>
        None
    }
  }
}
