package com.myway.sampleapp.redis
import cats.effect._
import com.myway.sampleapp.config.{AppConfigLoader, RedisConfig}

object RedisReader extends IOApp.Simple {
  def run: IO[Unit] = for {
    appConfig <- AppConfigLoader.loadConfig[IO]()
    _         <- read(appConfig.redisConfig)
  } yield ()

  def read(redisConfig: RedisConfig): IO[Unit] =
    RedisClient.commands[IO](redisConfig).use { cmd =>
      for {
        value <- cmd.get("example:key")
        _     <- IO.println(s"Read from Redis: $value")
      } yield ()
    }
}
