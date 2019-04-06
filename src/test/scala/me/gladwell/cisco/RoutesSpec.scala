package me.gladwell.cisco

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }

class RoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
  with Routes {

  lazy val routes = userRoutes

  "Routes" should {
    "return status OK" in {
      val request = HttpRequest(uri = "/")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
      }
    }

  }

}
