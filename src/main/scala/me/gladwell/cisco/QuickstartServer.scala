package me.gladwell.cisco

import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.Duration
import scala.util.{ Failure, Success }

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import Integer.parseInt

object QuickstartServer extends App with Routes {

  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  lazy val routes: Route = userRoutes
  lazy val port = Option(System.getProperty("http.port")).map(parseInt).getOrElse(8080)
  lazy val interface = Option(System.getProperty("http.interface")).getOrElse("localhost")

  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, interface, port)

  serverBinding.onComplete {
    case Success(bound) =>
      println(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
    case Failure(e) =>
      Console.err.println(s"Server could not start!")
      e.printStackTrace()
      system.terminate()
  }

  Await.result(system.whenTerminated, Duration.Inf)

}
