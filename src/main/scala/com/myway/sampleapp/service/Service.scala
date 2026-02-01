package com.myway.sampleapp.service

import cats.effect.Async
import cats.syntax.all._
import com.myway.sampleapp.redis.RedisReadWrite
import org.typelevel.log4cats.Logger

trait Service[F[_]] {

  def sampleService(arg: String): F[String]

  def redisWrite(key: String, value: String): F[Unit]
  def redisRead(key: String): F[Option[String]]
}

case class SampleService[F[_]: Async: Logger](
  redisReadWrite: RedisReadWrite[F]
) extends Service[F] {

  override def sampleService(arg: String): F[String] =
    Logger[F].info(s"Call to sample service: arg=$arg") *> Async[F].pure(arg.toUpperCase)

  override def redisWrite(key: String, value: String): F[Unit] =
    redisReadWrite.write(key, value)

  override def redisRead(key: String): F[Option[String]] =
    redisReadWrite.read(key)
}
