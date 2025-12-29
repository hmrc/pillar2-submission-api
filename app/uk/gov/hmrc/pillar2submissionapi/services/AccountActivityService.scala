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

package uk.gov.hmrc.pillar2submissionapi.services

import cats.data.EitherT
import cats.syntax.either.given
import play.api.Logging
import play.api.http.Status._
import play.api.libs.json.{JsError, JsSuccess, JsValue}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.connectors.AccountActivityConnector
import uk.gov.hmrc.pillar2submissionapi.controllers.error._
import uk.gov.hmrc.pillar2submissionapi.models.accountactivity.{AccountActivity, AccountActivityErrorResponse}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AccountActivityService @Inject() (accountActivityConnector: AccountActivityConnector)(using ExecutionContext) extends Logging {

  def retrieveAccountActivity(activityFromDate: LocalDate, activityToDate: LocalDate)(using
    HeaderCarrier
  ): EitherT[Future, Pillar2Error, AccountActivity] = EitherT {
    accountActivityConnector
      .getAccountActivity(activityFromDate = activityFromDate, activityToDate = activityToDate)
      .map(handleResponse)
  }

  private def handleResponse(response: HttpResponse): Either[Pillar2Error, AccountActivity] = response.status match {
    case OK =>
      Try(response.json).fold(
        error =>
          logger.error(s"Failed to parse json body from 200: $error")
          UnexpectedResponse.asLeft
        ,
        parseSuccess(_)
      )

    case BAD_REQUEST | UNAUTHORIZED | INTERNAL_SERVER_ERROR =>
      logger.warn(s"Expected error status ${response.status}, but mapping to 500.")
      UnexpectedResponse.asLeft

    case NOT_FOUND => AccountActivityNotFound.asLeft

    case UNPROCESSABLE_ENTITY =>
      Try(response.json).fold(
        error =>
          logger.error(s"Failed to parse unprocessable entity body from 422: $error")
          UnexpectedResponse.asLeft
        ,
        _.validate[AccountActivityErrorResponse] match {
          case JsSuccess(response, _) => DownstreamValidationError(code = response.code, message = response.message).asLeft
          case JsError(errors)        =>
            logger.error(s"Failed to parse unprocessable entity body from 422: $errors")
            UnexpectedResponse.asLeft
        }
      )

    case unexpectedStatus =>
      logger.error(s"Unexpected status $unexpectedStatus from account activity")
      UnexpectedResponse.asLeft
  }

  private def parseSuccess(body: JsValue): Either[Pillar2Error, AccountActivity] = ???

}
