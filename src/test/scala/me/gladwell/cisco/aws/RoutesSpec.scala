package me.gladwell.cisco.aws

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{ Matchers, WordSpec }
import spray.json._

import scala.concurrent.{ ExecutionContext, Future }

class RoutesSpec extends WordSpec with Matchers with ScalatestRouteTest with Routes {

  lazy val routes = userRoutes

  class MockInstanceFinder extends Instances {

    def forRegion(region: String)(implicit ec: ExecutionContext, key: AwsKey): Future[Seq[AwsInstance]] =
      Future.successful(
        Seq(
          AwsInstance(
            name = "b-123456abcd",
            instanceType = "t2.medium",
            state = "running",
            az = "us-east-1b",
            ip = "54.210.167.204",
            privateIp = "10.20.30.40"),
          AwsInstance(
            name = "a-123456abcd",
            instanceType = "t2.medium",
            state = "running",
            az = "eu-west-1d",
            ip = "54.210.167.204",
            privateIp = "10.20.30.40")))

  }

  override val instances = new MockInstanceFinder

  "Routes" should {
    val request = HttpRequest(uri = "/regions/eu-west-1/instances")
    val validCredentials = BasicHttpCredentials("aws-key-id", "aws-key")

    "return status OK" in {
      request ~> addCredentials(validCredentials) ~> routes ~> check {
        status should ===(StatusCodes.OK)
      }
    }

    "return JSON" in {
      request ~> addCredentials(validCredentials) ~> routes ~> check {
        mediaType should ===(MediaTypes.`application/json`)
      }
    }

    "return JSON array" in {
      request ~> addCredentials(validCredentials) ~> routes ~> check {
        responseAs[String].parseJson shouldBe a[JsArray]
      }
    }

    "return instance" in {
      request ~> addCredentials(validCredentials) ~> routes ~> check {
        val instances = responseAs[String].parseJson.asInstanceOf[JsArray].elements
        instances should contain(
          JsObject(
            Map(
              "name" -> JsString("a-123456abcd"),
              "instanceType" -> JsString("t2.medium"),
              "state" -> JsString("running"),
              "az" -> JsString("eu-west-1d"),
              "ip" -> JsString("54.210.167.204"),
              "privateIp" -> JsString("10.20.30.40"))))
      }
    }

    "sort by name by default" in {
      request ~> addCredentials(validCredentials) ~> routes ~> check {
        val instances = responseAs[String].parseJson.asInstanceOf[JsArray].elements
        val names = instances.map(_.asInstanceOf[JsObject].fields.get("name").get.toString())
        names should ===(names.sorted)
      }
    }

    "sort by attribute" in {
      val sortedRequest = HttpRequest(uri = "/regions/eu-west-1/instances?sort=az")
      sortedRequest ~> addCredentials(validCredentials) ~> routes ~> check {
        val instances = responseAs[String].parseJson.asInstanceOf[JsArray].elements
        val availabilityZones = instances.map(_.asInstanceOf[JsObject].fields.get("az").get.toString())
        availabilityZones should ===(availabilityZones.sorted)
      }
    }

  }

}
