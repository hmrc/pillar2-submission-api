package uk.gov.hmrc.pillar2submissionapi.controllers

import base.ControllerSpecBase
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsJson
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpVerbs.POST

class UktrSubmissionControllerSpec extends ControllerSpecBase {

    val validRequestJson: AnyContentAsJson = AnyContentAsJson(Json.parse("""{
                                            |  "accountingPeriodFrom": "2024-08-14",
                                            |  "accountingPeriodTo": "2024-12-14",
                                            |  "qualifyingGroup": true,
                                            |  "obligationDTT": true,
                                            |  "obligationMTT": true,
                                            |  "liabilities": {
                                            |    "totalLiability": 10000.99,
                                            |    "totalLiabilityDTT": 5000.99,
                                            |    "totalLiabilityIIR": 4000,
                                            |    "totalLiabilityUTPR": 10000.99,
                                            |    "liableEntities": [
                                            |      {
                                            |        "ukChargeableEntityName": "Newco PLC",
                                            |        "idType": "CRN",
                                            |        "idValue": "12345678",
                                            |        "amountOwedDTT": 5000,
                                            |        "electedDTT": true,
                                            |        "amountOwedIIR": 3400,
                                            |        "amountOwedUTPR": 6000.5,
                                            |        "electedUTPR": true
                                            |      }
                                            |    ]
                                            |  }
                                            |}""".stripMargin))

    val invalidRequestJson: AnyContentAsJson = AnyContentAsJson(Json.parse("""{
                                                                           |  "badRequest": ""
                                                                           |}""".stripMargin))

    "UktrSubmissionController" when {
        "Creating a new UKTR submission with a valid request body" should {
            val request = FakeRequest(POST,routes.UktrSubmissionController.submitUktr.url)
              .withBody[AnyContentAsJson](validRequestJson)

            "return a 200 OK response" in {
                val application = applicationBuilder().build()
                running(application) {
                    val result = route(application, request).value
                    status(result) mustEqual OK
                    }
                }
            }
        }

        "Creating a new UKTR submission with an invalid request body" should {
            val request = FakeRequest(POST,routes.UktrSubmissionController.submitUktr.url)
              .withBody[AnyContentAsJson](invalidRequestJson)

            "return a 400 BAD_REQUEST response" in {
                val application = applicationBuilder().build()
                running(application) {
                    val result = route(application, request).value
                    status(result) mustEqual BAD_REQUEST
                }
            }
        }

        "Creating a new UKTR submission with no request body" should {
            val request = FakeRequest(POST,routes.UktrSubmissionController.submitUktr.url)

            "return a 400 BAD_REQUEST response" in {
                val application = applicationBuilder().build()
                running(application) {
                    val result = route(application, request).value
                    status(result) mustEqual BAD_REQUEST
                }
            }
        }
    }

