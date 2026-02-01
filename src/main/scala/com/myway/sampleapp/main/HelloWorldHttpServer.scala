package com.myway.sampleapp.main

import cats.effect._
import com.myway.sampleapp.config.{AppConfigLoader, HttpServerConfig}
import com.myway.sampleapp.routes.HttpRoute
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration._
// Swagger: https://chatgpt.com/c/69791f56-1ec4-838e-b118-963d25004c79
object HelloWorldHttpServer extends IOApp {

  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  def server(httpConfig: HttpServerConfig): Resource[IO, Server] =
    EmberServerBuilder
      .default[IO]
      .withHost(httpConfig.host)
      .withPort(httpConfig.port)
      .withHttpApp(HttpRoute.httpApp[IO])
      .withShutdownTimeout(5.seconds)
      .build

  override def run(args: List[String]): IO[ExitCode] = for {
    appConfig <- AppConfigLoader.loadConfig[IO]()
    httpCfg   <- appConfig.getHttpServerConfig[IO]
    _ <- server(httpCfg).use(_ => logger.info(s"HTTP server started on ${appConfig}") >> IO.never)
  } yield ExitCode.Success

}
