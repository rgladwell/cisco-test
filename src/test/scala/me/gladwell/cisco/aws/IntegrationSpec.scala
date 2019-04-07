package me.gladwell.cisco.aws

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{ Authorization, BasicHttpCredentials }
import akka.http.scaladsl.model.{ HttpRequest, StatusCodes }
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, Matchers }

class IntegrationSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  val api = new Api()
  api.start()

  implicit lazy val ec = api.executionContext
  implicit lazy val actorSystem = api.system

  override def afterAll() {
    api.stop()
  }

  private def get(path: String = "") = {
    val credentials = BasicHttpCredentials("key-id", "secure-key")
    val request = HttpRequest(uri = api.uri + path).addHeader(Authorization(credentials))
    Http().singleRequest(request)
  }

  "should return status code" in {
    for {
      response <- get("regions/eu-west-1/instances")
    } yield assert(response.status == StatusCodes.InternalServerError)
  }

  "should return JSON" in {
    for {
      response <- get("regions/eu-west-1/instances")
    } yield {
      assert(response.entity.contentType.mediaType.toString == "application/json")
    }
  }

}
