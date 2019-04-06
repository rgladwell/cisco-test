package me.gladwell.cisco

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete

import akka.util.Timeout

trait Routes extends JsonSupport {

  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[Routes])

  implicit lazy val timeout = Timeout(5.seconds)

  lazy val userRoutes: Route =
    pathSingleSlash {
      concat(
        pathEnd {
          concat(
            get {
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
            })
        })
    }
}
