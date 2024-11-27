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

package uk.gov.hmrc.pillar2submissionapi.controllers.actions

import com.google.inject.{Inject, Singleton}
import play.api.Logging
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.models.requests.IdentifierRequest
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthenticatedIdentifierAction @Inject() (
  override val authConnector:    AuthConnector,
  val parser:                    BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends IdentifierAction
    with AuthorisedFunctions
    with Logging {

  val HMRC_PILLAR2_ORG_KEY = "HMRC-PILLAR2-ORG"
  val ENROLMENT_IDENTIFIER = "PLRID"

  override def refine[A](request: Request[A]): Future[Either[Result, IdentifierRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(AuthProviders(GovernmentGateway) and ConfidenceLevel.L50)
      .retrieve(
        Retrievals.internalId and Retrievals.groupIdentifier
          and Retrievals.allEnrolments and Retrievals.affinityGroup
          and Retrievals.credentialRole and Retrievals.credentials
      ) {

        case Some(internalId) ~ Some(groupId) ~ enrolments ~ Some(Organisation) ~ Some(User) ~ credentials
            if enrolments.getEnrolment(HMRC_PILLAR2_ORG_KEY).isDefined =>
          Future.successful(
            Right(
              IdentifierRequest(
                request = request,
                userId = internalId,
                groupId = Some(groupId),
                clientPillar2Id = enrolments.getEnrolment(HMRC_PILLAR2_ORG_KEY).get.getIdentifier(HMRC_PILLAR2_ORG_KEY).get.value,
                userIdForEnrolment = credentials.get.providerId
              )
            )
          )
        case _ => Future.successful(Left(Unauthorized))
      } recover { case _: AuthorisationException =>
      Left(Unauthorized)
    }
  }
}
