package com.myway.sampleapp.redis

import cats.effect.Async
import com.myway.sampleapp.config.RedisConfig
import org.typelevel.log4cats.Logger

trait RedisReadWrite[F[_]] {

  def write(key: String, value: String): F[Unit]
  def read(key: String): F[Option[String]]
}

object RedisReadWrite {

  def makeRedisReadWrite[F[_]: Async: Logger](redisConfig: RedisConfig): RedisReadWrite[F] =
    makeRedisReadWriteFromClient(RedisClient(redisConfig))
  def makeRedisReadWriteFromClient[F[_]: Async: Logger](
    redisClient: RedisClient[F]
  ): RedisReadWrite[F] =
    new RedisReadWrite[F] {

      private val redisWriter: RedisWriter[F] = RedisWriter(redisClient)
      private val redisReader                 = RedisReader(redisClient)

      override def write(key: String, value: String): F[Unit] = redisWriter.write(key, value)

      override def read(key: String): F[Option[String]] = redisReader.read(key)
    }
}
