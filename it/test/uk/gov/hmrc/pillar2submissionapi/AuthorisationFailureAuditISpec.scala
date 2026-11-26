/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.pillar2submissionapi

import com.github.tomakehurst.wiremock.client.WireMock.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.Application
import play.api.http.Status.*
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue
import play.api.mvc.Call
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Retrieval}
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments, User}
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pillar2submissionapi.base.IntegrationSpecBase
import uk.gov.hmrc.pillar2submissionapi.controllers.submission.routes
import uk.gov.hmrc.pillar2submissionapi.helpers.TestAuthRetrievals.~
import uk.gov.hmrc.pillar2submissionapi.models.response.Pillar2ErrorResponse
import uk.gov.hmrc.play.bootstrap.http.HttpClientV2Provider

import java.net.URI
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

class AuthorisationFailureAuditISpec extends IntegrationSpecBase with Eventually {

  override lazy val app: Application =
    guiceAppBuilder("auditing.enabled" -> true, "auditing.consumer.baseUri.port" -> wiremockPort).build()

  lazy val provider: HttpClientV2Provider = app.injector.instanceOf[HttpClientV2Provider]
  lazy val client:   HttpClientV2         = provider.get()

  private val returnsSubmissions: Seq[(String, Call)] = Seq(
    "submit UK tax return"                -> routes.UKTaxReturnController.submitUKTR,
    "amend UK tax return"                 -> routes.UKTaxReturnController.amendUKTR,
    "submit below-threshold notification" -> routes.BTNSubmissionController.submitBTN,
    "submit overseas return notification" -> routes.OverseasReturnNotificationController.submitORN,
    "amend overseas return notification"  -> routes.OverseasReturnNotificationController.amendORN
  )

  private def request(call: Call): RequestBuilder = {
    val url = URI.create(s"http://localhost:$port${call.url}").toURL
    if call.method == "PUT" then client.put(url) else client.post(url)
  }

  "A returns submission rejected while being authorised" must
    returnsSubmissions.foreach { case (name, call) =>
      s"return the error and send a Pillar2ApiAuthorisationFailure audit event for $name" in {
        server.stubFor(post(urlPathMatching("/write/audit.*")).willReturn(aResponse().withStatus(NO_CONTENT)))

        val otherEnrolments = Enrolments(Set(Enrolment("IR-CT", Seq(EnrolmentIdentifier("UTR", "1234567890")), "Activated", None)))
        when(
          mockAuthConnector.authorise[RetrievalsType](any[Predicate](), any[Retrieval[RetrievalsType]]())(using
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(
              Some(id) ~ Some(groupId) ~ otherEnrolments ~ Some(Organisation) ~ Some(User) ~ Some(Credentials(providerId, providerType))
            )
          )

        val result = Await.result(
          request(call)
            .setHeader("X-Pillar2-Id" -> plrReference, "Authorization" -> "bearerToken")
            .withBody(validLiabilityReturn)
            .execute[HttpResponse],
          5.seconds
        )

        result.status mustEqual FORBIDDEN
        result.json.as[Pillar2ErrorResponse].code mustEqual "INVALID_ENROLMENT"

        eventually(timeout(Span(5, Seconds)), interval(Span(100, Millis))) {
          server.verify(
            postRequestedFor(urlEqualTo("/write/audit"))
              .withRequestBody(matchingJsonPath("$.auditSource", equalTo("pillar2-submission-api")))
              .withRequestBody(matchingJsonPath("$.auditType", equalTo("Pillar2ApiAuthorisationFailure")))
              .withRequestBody(matchingJsonPath("$.tags.path", equalTo(call.url)))
              .withRequestBody(matchingJsonPath("$.detail.pillar2Id", equalTo(plrReference)))
              .withRequestBody(matchingJsonPath("$.detail.errorCode", equalTo("INVALID_ENROLMENT")))
              .withRequestBody(matchingJsonPath("$.detail.user.credId", equalTo(providerId)))
              .withRequestBody(matchingJsonPath("$.detail.user.enrolments[0]", equalTo("IR-CT:Activated")))
          )
        }
      }
    }
}
