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

  private def orderingFor(attribute: String)(i1: AwsInstance, i2: AwsInstance)(implicit ordering: Ordering[String]): Boolean = attribute match {
    case "name" => ordering.compare(i1.name, i2.name) < 0
    case "type" => ordering.compare(i1.instanceType, i2.instanceType) < 0
    case "state" => ordering.compare(i1.state, i2.state) < 0
    case "az" => ordering.compare(i1.az, i2.az) < 0
    case "ip" => ordering.compare(i1.ip, i2.ip) < 0
    case "privateIp" => ordering.compare(i1.privateIp, i2.privateIp) < 0
    case _ => false

  }

  private def page[T](maybeLimit: Option[Int], offset: Int, topage: Seq[T]): Seq[T] = {
    val offsetList = topage.drop(offset)
    maybeLimit match {
      case Some(limit) => offsetList.take(limit)
      case _ => offsetList
    }
  }

  lazy val userRoutes: Route =
    path("regions" / Segment / "instances") { region =>
      authenticate { implicit key =>
        parameters('sort ? "name", 'limit.as[Int].?, 'offset.as[Int] ? 0) { (attribute, maybeLimit, offset) =>
          get {
            onComplete(instances.forRegion(region)) {
              case Success(instances) => complete(page(maybeLimit, offset, instances.sortWith(orderingFor(attribute))))
              case Failure(ex) => complete(InternalServerError, ApiError(ex.getMessage()))
            }
          }
        }
      }
    }

}
