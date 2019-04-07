package me.gladwell.cisco.aws

import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.Duration
import scala.util.{ Failure, Success }

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import Integer.parseInt

class Api extends Routes {

  implicit val system: ActorSystem = ActorSystem("cisco-test-server")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  lazy val routes: Route = userRoutes
  lazy val port = Option(System.getProperty("http.port")).map(parseInt).getOrElse(8080)
  lazy val interface = Option(System.getProperty("http.interface")).getOrElse("localhost")

  lazy val uri = s"http://${interface}:${port}/"

  lazy val eventualBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, interface, port)

  override val instances = AwsInstances

  def start(): Future[Unit] = {
    eventualBinding onComplete {
      case Success(_) => println(s"Server online at $uri")
      case Failure(e) =>
        Console.err.println(s"Server could not start!")
        e.printStackTrace()
        system.terminate()
    }

    for {
      _ <- eventualBinding
    } yield ()
  }

  def stop(): Future[Unit] = {
    for {
      binding <- eventualBinding
      _ <- binding.unbind()
      _ <- system.terminate()
    } yield ()
  }

}

object Api extends Api with App {

  private val api = new Api()
  Await.result(api.start(), Duration.Inf)

}
