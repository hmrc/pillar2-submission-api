package uk.gov.hmrc.pillar2submissionapi.controllers.base

import uk.gov.hmrc.auth.core.retrieve.~

object TestAuthRetrievals {
  implicit class Ops[A](a: A) {
    def ~[B](b: B): A ~ B = new ~(a, b)
  }
}
