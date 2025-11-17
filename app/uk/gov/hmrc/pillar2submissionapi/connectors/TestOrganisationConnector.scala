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

package uk.gov.hmrc.pillar2submissionapi.connectors

import play.api.Logging
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.config.AppConfig
import uk.gov.hmrc.pillar2submissionapi.controllers.error.*
import uk.gov.hmrc.pillar2submissionapi.models.organisation.{TestOrganisation, TestOrganisationWithId}

import java.net.URI
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestOrganisationConnector @Inject() (
  val config: AppConfig,
  val http:   HttpClientV2
)(using ec: ExecutionContext)
    extends Logging {

  def createTestOrganisation(pillar2Id: String, request: TestOrganisation)(using hc: HeaderCarrier): Future[TestOrganisationWithId] = {
    val url = s"${config.stubBaseUrl}/pillar2/test/organisation/$pillar2Id"
    logger.info(s"Calling $url to create test organisation")

    http
      .post(URI.create(url).toURL)
      .withBody(Json.toJson(request))
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case 201 => Json.parse(response.body).as[TestOrganisationWithId]
          case 409 => throw OrganisationAlreadyExists(pillar2Id)
          case 500 => throw DatabaseError("create")
          case _   =>
            logger.warn(s"Unexpected response from create organisation with status: ${response.status}")
            throw UnexpectedResponse
        }
      }
  }

  def getTestOrganisation(pillar2Id: String)(using hc: HeaderCarrier): Future[TestOrganisationWithId] = {
    val url = s"${config.stubBaseUrl}/pillar2/test/organisation/$pillar2Id"
    logger.info(s"Calling $url to get test organisation")

    http
      .get(URI.create(url).toURL)
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case 200 => Json.parse(response.body).as[TestOrganisationWithId]
          case 404 => throw OrganisationNotFound(pillar2Id)
          case _   =>
            logger.warn(s"Unexpected response from get organisation with status: ${response.status}")
            throw UnexpectedResponse
        }
      }
  }

  def updateTestOrganisation(pillar2Id: String, request: TestOrganisation)(using hc: HeaderCarrier): Future[TestOrganisationWithId] = {
    val url = s"${config.stubBaseUrl}/pillar2/test/organisation/$pillar2Id"
    logger.info(s"Calling $url to update test organisation")

    http
      .put(URI.create(url).toURL)
      .withBody(Json.toJson(request))
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case 200 => Json.parse(response.body).as[TestOrganisationWithId]
          case 404 => throw OrganisationNotFound(pillar2Id)
          case 500 => throw DatabaseError("update")
          case _   =>
            logger.warn(s"Unexpected response from update organisation with status: ${response.status}")
            throw UnexpectedResponse
        }
      }
  }

  def deleteTestOrganisation(pillar2Id: String)(using hc: HeaderCarrier): Future[Unit] = {
    val url = s"${config.stubBaseUrl}/pillar2/test/organisation/$pillar2Id"
    logger.info(s"Calling $url to delete test organisation")

    http
      .delete(URI.create(url).toURL)
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case 204 => ()
          case 404 => throw OrganisationNotFound(pillar2Id)
          case 500 => throw DatabaseError("Failed to delete organisation and submission data")
          case _   =>
            logger.warn(s"Unexpected response from delete organisation with status: ${response.status}")
            throw UnexpectedResponse
        }
      }
  }
}
