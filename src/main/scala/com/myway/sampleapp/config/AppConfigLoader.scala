package com.myway.sampleapp.config

import cats.effect.Async
import com.comcast.ip4s.{Host, Port}
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax.CatsEffectConfigSource

case class HttpServerConfig(
  host: Host,
  port: Port
)
final case class HttpConfig(
  host: String,
  port: Int
) {
  def toServerConfig: Either[String, HttpServerConfig] =
    for {
      h <- Host.fromString(host).toRight(s"Invalid host: $host")
      p <- Port.fromInt(port).toRight(s"Invalid port: $port")
    } yield HttpServerConfig(h, p)
}

final case class AppConfig(httpConfig: HttpConfig) {

  private def makeHttpServerConfigOpt: Option[HttpServerConfig] =
    for {
      h <- Host.fromString(httpConfig.host)
      p <- Port.fromInt(httpConfig.port)
    } yield HttpServerConfig(h, p)

  def fromOption[F[_]: Async, A](opt: Option[A], error: => Throwable): F[A] =
    opt match {
      case Some(value) => Async[F].pure(value)
      case None        => Async[F].raiseError(error)
    }
  def getHttpServerConfig[F[_]: Async]: F[HttpServerConfig] =
    fromOption(makeHttpServerConfigOpt, new RuntimeException)
}
object AppConfigLoader {
  def loadConfig[F[_]: Async](
    source: ConfigSource = ConfigSource.resources("application.yml")
  ): F[AppConfig] =
    Async[F].map(source.loadF[F, HttpConfig]())(AppConfig(_))

}
