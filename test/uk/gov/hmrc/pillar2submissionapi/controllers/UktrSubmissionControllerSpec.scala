package uk.gov.hmrc.pillar2submissionapi.controllers

import base.ControllerBaseSpec
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsJson
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import uk.gov.hmrc.http.HttpVerbs.POST

class UktrSubmissionControllerSpec extends ControllerBaseSpec {

  val uktrSubmissionController: UktrSubmissionController = new UktrSubmissionController(cc)
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

  "UktrSubmissionController" when {
    "submitUktr()" in {
      val result = uktrSubmissionController.submitUktr()(FakeRequest(method = POST, path = "/test")
        .withBody(validRequestJson))
      status(result) mustEqual OK
    }
  }

}
