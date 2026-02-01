package com.myway.sampleapp.redis
import cats.effect._
import cats.syntax.all._
import com.myway.sampleapp.config.{AppConfigLoader, RedisConfig}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
object RedisWriter extends IOApp.Simple {

  def run: IO[Unit] = for {
    appConfig <- AppConfigLoader.loadConfig[IO]()
    _         <- write(appConfig.redisConfig)
  } yield ()

  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  def write(redisConfig: RedisConfig): IO[Unit] =
    RedisWriter[IO](RedisClient[IO](redisConfig))
      .write("example:key", " ---- *** Hello from Program 4 *** ----")

}

case class RedisWriter[F[_]: Async: Logger](redisClient: RedisClient[F]) {

  def write(key: String, value: String): F[Unit] =
    redisClient.commands.use { cmd =>
      for {
        _ <- cmd.set(key, value)
        _ <- Logger[F].info(s"Value written to Redis on key:$key")
      } yield ()
    }

}
