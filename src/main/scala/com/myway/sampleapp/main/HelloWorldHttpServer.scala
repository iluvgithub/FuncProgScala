package com.myway.sampleapp.main

import cats.effect._
import com.myway.sampleapp.routes.HttpRoute
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration._

// Swagger: https://chatgpt.com/c/69791f56-1ec4-838e-b118-963d25004c79
object HelloWorldHttpServer extends IOApp {

  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  // Server resource
  def server: Resource[IO, Server] =
    EmberServerBuilder
      .default[IO]
      .withHost(com.comcast.ip4s.Hostname.fromString("0.0.0.0").get)
      .withPort(com.comcast.ip4s.Port.fromInt(8080).get)
      .withHttpApp(HttpRoute.httpApp[IO])
      .withShutdownTimeout(5.seconds)
      .build

  override def run(args: List[String]): IO[ExitCode] =
    server
      .use { _ =>
        logger.info("HTTP server started on http://localhost:8080") >>
          IO.never
      }
      .as(ExitCode.Success)
}
