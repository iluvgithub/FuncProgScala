package com.myway.catseffect.sync
import cats.Parallel
import cats.effect.{Async, IO, IOApp}

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneOffset}
object AsyncIODelay extends IOApp.Simple {

  import cats.effect.{Sync, Temporal}
  import cats.syntax.all._

  import scala.concurrent.duration._
  import scala.util.Random

  def randomSleepMicros[F[_]: Async: Temporal]: F[Int] =
    for {
      micros <- Sync[F].delay {
        Random.nextInt(800) // fast sync work (< 1 ms)
      }
      _ <- Temporal[F].sleep(micros.millis) // ← proper async fiber sleep
      _ <- Sync[F].delay {
        println(s"slept $micros ms on ${Thread.currentThread().getName}")
      }
    } yield micros

  def program1[F[_]: Async : Parallel]: F[Int] =
    (1 to 12).toList
      .map(_ => randomSleepMicros)
      //  .parSequence
      .sequence
      .map(_.sum)

  def io(prefix: String): IO[LocalDateTime] = for {
    now <- IO(java.time.LocalDateTime.now())
    _   <- IO.println(s"$prefix:${DateTimeFormatter.ofPattern("hh.mm.ss.SSS").format(now)}")
  } yield now

  val run: IO[Unit] = for {
    begin <- io("BEGIN")
    total <- program1[IO]
    end   <- io("END")
    out <- IO.println(
      s"Elapsed:${end.toInstant(ZoneOffset.UTC).toEpochMilli - begin.toInstant(ZoneOffset.UTC).toEpochMilli} total:$total"
    )
  } yield ()

}
