package me.gladwell.cisco.aws

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val errorFormat = jsonFormat1(ApiError)
  implicit val instanceFormat = jsonFormat6(AwsInstance)

}
