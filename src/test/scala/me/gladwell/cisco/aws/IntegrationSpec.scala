package me.gladwell.cisco.aws

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpRequest, StatusCodes }
import org.scalatest.{ BeforeAndAfterAll, Matchers, AsyncWordSpec }

class IntegrationSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  val api = new Api()
  api.start()

  implicit lazy val ec = api.executionContext
  implicit lazy val actorSystem = api.system

  override def afterAll() {
    api.stop()
  }

  private def get(path: String = "") = {
    Http().singleRequest(HttpRequest(uri = api.uri + path))
  }

  "should return status code" in {
    for {
      response <- get("regions/eu-west-1/instances")
    } yield assert(response.status == StatusCodes.Unauthorized)
  }

  "should return JSON" in {
    for {
      response <- get("regions/eu-west-1/instances")
    } yield assert(response.entity.contentType.mediaType.toString == "application/json")
  }

}
