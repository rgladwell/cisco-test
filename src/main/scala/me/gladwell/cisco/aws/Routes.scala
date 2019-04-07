package me.gladwell.cisco.aws

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.directives.FutureDirectives.onComplete
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

trait Routes extends JsonSupport {

  implicit def system: ActorSystem
  implicit lazy val executionContext: ExecutionContext = system.dispatcher

  val instances: Instances

  private def authenticator(credentials: Credentials): Option[Unit] = credentials match {
    case p @ Credentials.Provided(id) => Some(id)
    case _ => None
  }

  lazy val userRoutes: Route =
    path("regions" / Segment / "instances") { region =>
      // TODO for added security verify we're using TLS here
      authenticateBasic(realm = "secure site", authenticator) { _ =>
        extractCredentials { credentials =>
          get {
            credentials match {
              case Some(BasicHttpCredentials(id, key)) => {
                implicit val awsKey = AwsKey(id, key)
                onComplete(instances.forRegion(region)) {
                  case Success(instances) => complete(instances)
                  case Failure(ex) => {
                    ex.printStackTrace()
                    complete(InternalServerError, ApiError(ex.getMessage()))
                  }
                }
              }
              case None => complete(Unauthorized, ApiError("invalid credentials"))
            }
          }
        }
      }
    }

}
