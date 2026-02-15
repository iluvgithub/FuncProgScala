package com.myway.sampleapp.service

import cats.effect.kernel.{Concurrent, Temporal}
import cats.effect.{ExitCode, IO, IOApp}
import fs2._
import fs2.concurrent.SignallingRef

import scala.concurrent.duration.DurationInt

object SignallingRefSandbox extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    def stream(signal: SignallingRef[IO, Boolean]): Stream[IO, Int] =
      Stream[IO, Int](1, 2, 3, 4, 5, 6, 7, 8, 9, 100, 100)
        .evalMap(x => signal.get.flatMap(b => IO(println(s"x=$x,b=$b"))) >> IO(x))
        .metered(300.millis)
        .interruptWhen(signal.discrete)

    def stream2(signal: SignallingRef[IO, Boolean]): Stream[IO, Int] =
      Stream
        .eval(IO.sleep(2.second) >> signal.set(true) >> IO(println("STOP")) >> IO(1))
        .repeat
        .interruptWhen(signal.discrete)
    val ioSignal: IO[SignallingRef[IO, Boolean]] = SignallingRef(false)

    val io: IO[Unit] =
      ioSignal.flatMap(signal => stream(signal).merge(stream2(signal)).compile.drain)
    io >> IO(ExitCode.Success)
  }
}
object Merge extends IOApp {

  private def helloWords0[F[_]]: Stream[F, String] =
    Stream[F, String]("Hello", "Cádiz", "Spain", "South", "Sun").map(x => s"_$x")
  private def goodbyeWords0[F[_]] =
    Stream[F, String]("Goodbye", "London", "UK", "North", "Rain", "Next")

  private def helloWords[F[_]: Temporal]   = helloWords0.metered(150.millis)
  private def goodbyeWords[F[_]: Temporal] = goodbyeWords0.metered(50.millis)

  override def run(args: List[String]): IO[ExitCode] = {

    val stream: Stream[IO, String] = program(helloWords[IO], goodbyeWords[IO])

    stream.foreach(IO.println).compile.drain.as(ExitCode.Success) >>
      IO(ExitCode.Success)
  }

  def program[F[_]: Temporal, A](left: Stream[F, A], right: Stream[F, A]): Stream[F, A] =
    // left.interleave(right)
    left merge right
}
