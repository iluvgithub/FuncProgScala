package com.myway.catseffect.sync
import cats.effect.{IO, IOApp, Sync}

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneOffset}

object SyncIODelay extends IOApp.Simple {
  import scala.util.Random
  def randomSleepMicros: IO[Int] =
    Sync[IO].delay { // This is fast – usually < 1 ms
      //  Sync[IO].blocking {
      val micros: Int = Random.nextInt(800)
      Thread.sleep(micros) // millis precision sleep
      println(s"slept $micros ms on ${Thread.currentThread().getName}")
      micros
    }

  val program1: IO[Int] =
    (1 to 12).toList
      .map(_ => randomSleepMicros)
      .parSequence
      //  .sequence
      .map(_.sum)

  def io(prefix: String): IO[LocalDateTime] = for {
    now <- IO(java.time.LocalDateTime.now())
    _   <- IO.println(s"$prefix:${DateTimeFormatter.ofPattern("hh.mm.ss.SSS").format(now)}")
  } yield now

  val run: IO[Unit] = for {
    begin <- io("BEGIN")
    total <- program1
    end   <- io("END")
    out <- IO.println(
      s"Elapsed:${end.toInstant(ZoneOffset.UTC).toEpochMilli - begin.toInstant(ZoneOffset.UTC).toEpochMilli} total:$total"
    )
  } yield ()

}
