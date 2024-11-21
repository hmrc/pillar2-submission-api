package uk.gov.hmrc.pillar2submissionapi.controllers.actions

import play.api.mvc.{ActionBuilder, ActionFunction, ActionRefiner, AnyContent, Request}
import uk.gov.hmrc.pillar2submissionapi.models.requests.IdentifierRequest

trait IdentifierAction
  extends ActionRefiner[Request, IdentifierRequest]
    with ActionBuilder[IdentifierRequest, AnyContent]
    with ActionFunction[Request, IdentifierRequest]
