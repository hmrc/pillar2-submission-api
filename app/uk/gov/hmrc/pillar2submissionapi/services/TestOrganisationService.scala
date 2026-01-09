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

package uk.gov.hmrc.pillar2submissionapi.services

import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pillar2submissionapi.connectors.TestOrganisationConnector
import uk.gov.hmrc.pillar2submissionapi.models.organisation.{TestOrganisation, TestOrganisationRequest, TestOrganisationWithId}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class TestOrganisationService @Inject() (
  testOrganisationConnector: TestOrganisationConnector
) extends Logging {

  def createTestOrganisation(pillar2Id: String, request: TestOrganisationRequest)(implicit
    hc: HeaderCarrier
  ): Future[TestOrganisationWithId] = {
    val organisationDetails = TestOrganisation(request.orgDetails, request.accountingPeriod, request.testData)
    testOrganisationConnector.createTestOrganisation(pillar2Id, organisationDetails)
  }

  def getTestOrganisation(pillar2Id: String)(using hc: HeaderCarrier): Future[TestOrganisationWithId] =
    testOrganisationConnector.getTestOrganisation(pillar2Id)

  def updateTestOrganisation(pillar2Id: String, request: TestOrganisationRequest)(implicit
    hc: HeaderCarrier
  ): Future[TestOrganisationWithId] = {
    val organisationDetails = TestOrganisation(request.orgDetails, request.accountingPeriod, request.testData)
    testOrganisationConnector.updateTestOrganisation(pillar2Id, organisationDetails)
  }

  def deleteTestOrganisation(pillar2Id: String)(using hc: HeaderCarrier): Future[Unit] =
    testOrganisationConnector.deleteTestOrganisation(pillar2Id)
}
