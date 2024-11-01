package uk.gov.hmrc.pillar2submissionapi.controllers

import base.ControllerBaseSpec
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, AnyContentAsJson}
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import uk.gov.hmrc.pillar2submissionapi.controllers.UktrSubmissionControllerSpec._

class UktrSubmissionControllerSpec extends ControllerBaseSpec {

  val uktrSubmissionController: UktrSubmissionController = new UktrSubmissionController(cc)

  "UktrSubmissionController" when {
    "submitUktr() called with valid request" in {
      val result = uktrSubmissionController.submitUktr()(FakeRequest(method = "", path = "")
        .withBody(validRequestJson))
      status(result) mustEqual OK
    }

    "submitUktr() called with invalid requests" in {
      val requestTemplate = FakeRequest(method = "", path = "")
      val testCases = Table(
        ("input", "expectedResponse"),
        (requestTemplate.withBody(invalidRequest_Json), BAD_REQUEST),
        (requestTemplate.withBody(invalidRequest_wrongType), BAD_REQUEST),
        (requestTemplate.withBody(invalidRequest_noBody), BAD_REQUEST))

      forAll(testCases) { (input: FakeRequest[Any], expectedResponse: Int) =>
        val result = uktrSubmissionController.submitUktr()(input)
          status(result) mustEqual expectedResponse
      }
    }
  }
}

object UktrSubmissionControllerSpec {
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

  val invalidRequest_Json: AnyContentAsJson = AnyContentAsJson(Json.parse("""{
                                                                            |  "badRequest": ""
                                                                            |}""".stripMargin))

  val invalidRequest_wrongType: AnyContent = AnyContent("This is not Json.")
  val invalidRequest_noBody: Unit = ()
}
