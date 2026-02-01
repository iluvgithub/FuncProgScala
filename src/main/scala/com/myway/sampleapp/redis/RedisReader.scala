package com.myway.sampleapp.redis
import cats.effect._
import cats.syntax.all._
import com.myway.sampleapp.config.{AppConfigLoader, RedisConfig}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
object RedisReader extends IOApp.Simple {
  def run: IO[Unit] = for {
    appConfig <- AppConfigLoader.loadConfig[IO]()
    _         <- read(appConfig.redisConfig)
  } yield ()
  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  private def read(redisConfig: RedisConfig): IO[Option[String]] =
    RedisReader[IO](RedisClient[IO](redisConfig)).read("example:key")
}

case class RedisReader[F[_]: Async: Logger](redisClient: RedisClient[F]) {

  def read(key: String): F[Option[String]] =
    redisClient.commands.use { cmd =>
      for {
        value <- cmd.get(key)
        _     <- Logger[F].info(s"Read from Redis: $value")
      } yield value
    }
}
