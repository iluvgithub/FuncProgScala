package com.myway.sampleapp.service

import cats.effect.Async
import org.typelevel.log4cats.Logger

trait Service[F[_]] {

  def sampleService(arg: String): F[String]

  def redisWrite(key: String, value: String): F[Unit]
  def redisRead(key: String): F[String]
}

case class SampleService[F[_]: Async: Logger]() extends Service[F] {

  override def sampleService(arg: String): F[String] =
    Async[F].pure(arg.toUpperCase)

  override def redisWrite(key: String, value: String): F[Unit] = Async[F].pure(())

  override def redisRead(key: String): F[String] = Async[F].pure("redis read")
}
