package com.myway.sampleapp.redis
import cats.effect._
import com.myway.sampleapp.config.{AppConfigLoader, RedisConfig}

object RedisWriter extends IOApp.Simple {

  def run: IO[Unit] = for {
    appConfig <- AppConfigLoader.loadConfig[IO]()
    _         <- write(appConfig.redisConfig)
  } yield ()

  def write(redisConfig: RedisConfig): IO[Unit] =
    RedisClient.commands[IO](redisConfig).use { cmd =>
      for {
        _ <- cmd.set("example:key", " ---- *** Hello from Program 4 *** ----")
        _ <- IO.println("Value written to Redis")
      } yield ()
    }
}
