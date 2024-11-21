package uk.gov.hmrc.pillar2submissionapi.controllers.actions.base

import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.pillar2submissionapi.controllers.actions.IdentifierAction
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

trait ActionBaseSpec extends PlaySpec with MockitoSugar {

  val identifierAction:   IdentifierAction = mock[IdentifierAction]
  val mockAuditConnector: AuditConnector   = mock[AuditConnector]
}
