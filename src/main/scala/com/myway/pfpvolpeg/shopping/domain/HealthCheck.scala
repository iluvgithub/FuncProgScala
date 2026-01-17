package com.myway.pfpvolpeg.shopping.domain

trait HealthCheck[F[_]] {
  def status: F[AppStatus]
}

case class RedisStatus(value: Status)
case class PostgresStatus(value: Status)
case class AppStatus(
  redis: RedisStatus,
  postgres: PostgresStatus
)
sealed trait Status

case object Okay extends Status

case object Unreachable extends Status
