package com.myway.sampleapp.redis
import cats.effect._
import com.myway.sampleapp.config.RedisConfig
import dev.profunktor.redis4cats._
import dev.profunktor.redis4cats.effect.Log.Stdout._
import dev.profunktor.redis4cats.RedisCommands


// sudo systemctl start redis-server
// redis-cli -h localhost -p  6379

object RedisClient {

  def commands[F[_]: Async](redisConfig:RedisConfig): Resource[F, RedisCommands[F, String, String]] =
    Redis[F].utf8(s"redis://${redisConfig.host}:${redisConfig.port}")
}
