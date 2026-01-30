package com.myway.sampleapp.routes

import cats.effect._
import cats.syntax.all._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.Router
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object HttpRoute {

  implicit def logger[F[_] : Sync]: Logger[F] =
    Slf4jLogger.getLogger[F]

  def routes[F[_] : Async : Logger]: HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {

      case req@GET -> Root / "health" =>
        Logger[F].info(s"GET ${req.uri}") *>
          Ok("OK")

      case req@GET -> Root / "hello" =>
        Logger[F].info(s"GET ${req.uri}") *>
          Ok("Hello world")

      case req@GET -> Root / "service" =>
        req.params.get("arg") match {
          case Some(arg) => Ok(s"Received arg = $arg")
          case None => BadRequest("Missing query parameter arg1")
        }

      case req@GET -> Root / "service" / "subservice" =>
        Logger[F].info(s"GET ${req.uri}") *>
          Ok("subservice ")

      case req@GET -> Root / "service"/ "subservice2" =>
        Logger[F].info(s"GET ${req.uri}") *>
          Ok("subservice 2")

      case req@GET -> Root   =>
        Logger[F].info(s"GET ${req.uri}") *>
          Ok("Root")

      // POST /echo
      case req@POST -> Root / "echo" =>
        for {
          body <- req.as[String]
          _ <- Logger[F].info(
            s"POST ${req.uri} | body=$body"
          )
          res <- Ok(s"Received: ${body.toUpperCase}")
        } yield res
    }
  }

  def httpApp[F[_] : Async : Logger]: HttpApp[F] =
    Router("/" -> routes[F]).orNotFound

}

