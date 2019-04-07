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

  // TODO for added security verify we're using TLS here
  def authenticate(subRoute: AwsKey => Route) = {

    def authenticator(credentials: Credentials): Option[String] = credentials match {
      case Credentials.Provided(id) => Some(id)
      case _ => None
    }

    authenticateBasic(realm = "cisco-aws-ec2-api", authenticator) { _ =>
      extractCredentials {
        case Some(BasicHttpCredentials(id, key)) => subRoute(AwsKey(id, key))
        case None => complete(Unauthorized, ApiError("invalid credentials"))
      }
    }

  }

  lazy val userRoutes: Route =
    path("regions" / Segment / "instances") { region =>
      authenticate { implicit key =>
        get {
          onComplete(instances.forRegion(region)) {
            case Success(instances) => complete(instances)
            case Failure(ex) => complete(InternalServerError, ApiError(ex.getMessage()))
          }
        }
      }
    }

}
