package com.myway.sampleapp.config

import cats.effect.Async
import com.comcast.ip4s.{Host, Port}
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax.CatsEffectConfigSource
case class HttpServerConfig(
  host: Host,
  port: Port
)
final case class HttpConfig(
  host: String,
  port: Int
)

case class RedisConfig(
  host: String,
  port: Int
)
final case class AppConfig(httpConfig: HttpConfig, redisConfig:RedisConfig) {

  private def makeHttpServerConfigOpt: Option[HttpServerConfig] =
    for {
      h <- Host.fromString(httpConfig.host)
      p <- Port.fromInt(httpConfig.port)
    } yield HttpServerConfig(h, p)

  private def fromOption[F[_]: Async, A](opt: Option[A], error: => Throwable): F[A] =
    opt match {
      case Some(value) => Async[F].pure(value)
      case None        => Async[F].raiseError(error)
    }
  def getHttpServerConfig[F[_]: Async]: F[HttpServerConfig] =
    fromOption(makeHttpServerConfigOpt, new RuntimeException)
}

object AppConfigLoader {
  def loadConfig[F[_]: Async](
    source: ConfigSource = ConfigSource.resources("application.conf")
  ): F[AppConfig] = source.loadF[F, AppConfig]

}
