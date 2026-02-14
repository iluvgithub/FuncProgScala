package com.myway.sampleapp.redis.lock
import cats.Monad
import cats.effect.implicits.monadCancelOps_
import cats.effect.std.Console
import cats.effect.{Async, IO, IOApp, Resource}
import cats.syntax.all._
import dev.profunktor.redis4cats.effect.Log
import dev.profunktor.redis4cats.effects._
import dev.profunktor.redis4cats.{Redis, RedisCommands}

import java.util.UUID
import scala.concurrent.duration._

object RedisLock {

  def acquire[F[_]: Monad](
    redis: RedisCommands[F, String, String],
    lockKey: String,
    ttl: FiniteDuration = 15.seconds
  ): F[Option[String]] = { // returning String (the unique owner id)
    val uniqueValue = UUID.randomUUID().toString
    redis
      .setNx(lockKey, uniqueValue)
      .flatMap { acquired =>
        if (acquired) redis.expire(lockKey, ttl).as(uniqueValue.some)
        else none[String].pure[F]
      }
  }

  /** Atomic release: only delete if the value still matches our owner id */
  def release[F[_]: Monad](
    redis: RedisCommands[F, String, String],
    lockKey: String,
    owner: String
  ): F[Boolean] = {
    val script =
      """
        |if redis.call("GET", KEYS[1]) == ARGV[1] then
        |    return redis.call("DEL", KEYS[1])
        |else
        |    return 0
        |end
        |""".stripMargin
    redis
      .eval(script, ScriptOutputType.Integer[String], List(lockKey), List(owner))
      .map(_ == 1L)
  }

  /** Bracket-style: acquire → use → release (guaranteed) */
  def guarded[F[_]: Async: Console, A](
    redis: RedisCommands[F, String, String],
    lockKey: String,
    ttl: FiniteDuration = 15.seconds
  )(use: F[A]): Resource[F, Option[A]] =
    Resource.eval(acquire(redis, lockKey, ttl)).evalMap {
      case None => none[A].pure[F]
      case Some(owner) =>
        use
          .guarantee {
            release(redis, lockKey, owner).flatMap { released =>
              if (!released)
                Console[F].println(s"Lock $lockKey was expired or stolen!").void
              else
                ().pure[F]
            }
          }
          .map(_.some)
    }
}
object RedisLockDemo extends IOApp.Simple {

  // ────────────────────────────────────────────────────────────────
  //   Simple distributed lock helper (single Redis instance)
  // ────────────────────────────────────────────────────────────────

  // ────────────────────────────────────────────────────────────────
  //   Main demo: two "clients" incrementing a shared counter
  // ────────────────────────────────────────────────────────────────   // ← this fixes the error
  val program: IO[Unit] = {

    val nbStepsPerWorker: Int = 10
    implicit val log: Log[IO] = Log.NoOp.instance[IO]
    Redis[IO].utf8("redis://localhost:6379").use { redis =>
      val dataKey = "counter"
      val lockKey = s"lock:$dataKey"

      def worker(id: String): IO[Unit] = {

        def incrementOnce: IO[Unit] =
          RedisLock
            .guarded(redis, lockKey, 10.seconds)(
              for {
                currentStr <- redis.get(dataKey)
                current  = currentStr.flatMap(_.toIntOption).getOrElse(0)
                newValue = current + 1
                _ <- redis.set(dataKey, newValue.toString)
                _ <- IO.println(s"[$id] $current → $newValue")
                _ <- IO.sleep(50.millis) // simulate work
              } yield ()
            )
            .use {
              case Some(_) => ().pure[IO]
              case None =>
                IO.println(s"[$id] Could not acquire lock → retrying") >>
                  IO.sleep(30.millis) >>
                  incrementOnce // naive retry (add backoff in prod)
            }

        incrementOnce.replicateA(nbStepsPerWorker).void
      }

      for {
        _ <- redis.del(dataKey)
        _ <- redis.set(dataKey, "0")
        _ <- IO.println("Reset counter to 0. Starting two workers...")

        worker1 = worker("Client-A")
        worker2 = worker("Client-B")

        f1 <- worker1.start
        f2 <- worker2.start

        _ <- f1.join
        _ <- f2.join

        final0 <- redis.get(dataKey)
        _ <- IO.println(
          s"Final counter = ${final0.getOrElse("???")}  (expected: ${nbStepsPerWorker * 2})"
        )
      } yield ()
    }
  }

  val run: IO[Unit] = program
}
