package com.myway.sampleapp.redis
import cats.effect._
import com.myway.sampleapp.config.RedisConfig
import dev.profunktor.redis4cats._
import dev.profunktor.redis4cats.effect.Log.Stdout._

// sudo systemctl start redis-server
// redis-cli -h localhost -p  6379

case class RedisClient[F[_]: Async](redisConfig: RedisConfig) {

  val commands: Resource[F, RedisCommands[F, String, String]] =
    Redis[F].utf8(s"redis://${redisConfig.host}:${redisConfig.port}")
}
