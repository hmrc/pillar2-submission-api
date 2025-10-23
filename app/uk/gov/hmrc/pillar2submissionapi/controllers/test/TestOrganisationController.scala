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

package uk.gov.hmrc.pillar2submissionapi.controllers.test

import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.config.AppConfig
import uk.gov.hmrc.pillar2submissionapi.controllers.actions.{IdentifierAction, Pillar2IdHeaderExistsAction}
import uk.gov.hmrc.pillar2submissionapi.controllers.error._
import uk.gov.hmrc.pillar2submissionapi.models.organisation.TestOrganisationRequest
import uk.gov.hmrc.pillar2submissionapi.services.TestOrganisationService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestOrganisationController @Inject() (
  cc:                      ControllerComponents,
  identify:                IdentifierAction,
  pillar2IdAction:         Pillar2IdHeaderExistsAction,
  testOrganisationService: TestOrganisationService,
  config:                  AppConfig
)(using ec: ExecutionContext)
    extends BackendController(cc) {

  private def checkTestEndpointsEnabled[A](block: => Future[A]): Future[A] =
    if (config.testOrganisationEnabled) block else Future.failed(TestEndpointDisabled)

  def createTestOrganisation: Action[AnyContent] = (pillar2IdAction andThen identify).async { request =>
    given HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    checkTestEndpointsEnabled {
      request.body.asJson match {
        case Some(json) =>
          json.validate[TestOrganisationRequest] match {
            case JsSuccess(value, _) =>
              if (!value.accountingPeriod.endDate.isAfter(value.accountingPeriod.startDate)) Future.failed(InvalidDateRange)
              else
                testOrganisationService
                  .createTestOrganisation(request.clientPillar2Id, value)
                  .map(response => Created(Json.toJson(response)))

            case JsError(_) => Future.failed(InvalidJson)
          }
        case None => Future.failed(EmptyRequestBody)
      }
    }
  }

  def getTestOrganisation: Action[AnyContent] = (pillar2IdAction andThen identify).async { request =>
    given HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    checkTestEndpointsEnabled {
      testOrganisationService
        .getTestOrganisation(request.clientPillar2Id)
        .map(response => Ok(Json.toJson(response)))
    }
  }

  def updateTestOrganisation: Action[AnyContent] = (pillar2IdAction andThen identify).async { request =>
    given HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    checkTestEndpointsEnabled {
      request.body.asJson match {
        case Some(json) =>
          json.validate[TestOrganisationRequest] match {
            case JsSuccess(value, _) =>
              if (!value.accountingPeriod.endDate.isAfter(value.accountingPeriod.startDate)) Future.failed(InvalidDateRange)
              else
                testOrganisationService
                  .updateTestOrganisation(request.clientPillar2Id, value)
                  .map(response => Ok(Json.toJson(response)))
            case JsError(_) => Future.failed(InvalidJson)
          }
        case None => Future.failed(EmptyRequestBody)
      }
    }
  }

  def deleteTestOrganisation: Action[AnyContent] = (pillar2IdAction andThen identify).async { request =>
    given HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    checkTestEndpointsEnabled {
      testOrganisationService
        .deleteTestOrganisation(request.clientPillar2Id)
        .map(_ => NoContent)
    }
  }
}
