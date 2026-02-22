package com.myway.io.tagless

import cats.Monad
import cats.effect.IO
import cats.effect.std.Console
import cats.implicits.{catsSyntaxApplicativeByValue, toFlatMapOps, toFunctorOps}
import munit.CatsEffectSuite

class CounterTest extends CatsEffectSuite {

  case class CounterProgram[F[_]: Monad: Console](counter: Counter[F]) {
    def program: F[Int] =
      for {
        _   <- display(counter)
        _   <- counter.increment
        _   <- display(counter)
        _   <- repeat(5)(counter.increment)
        out <- counter.get
        _   <- display(counter)
      } yield out
    def display(counter: Counter[F]): F[Unit] =
      counter.get.flatMap(Console[F].println)

    def repeat(n: Int)(action: F[Unit]): F[Unit] =
      action.replicateA(n).void
  }
  test("increment and get on in memory counter") {
    // arrange

    // act
    val io: IO[Int] = Counter.make[IO].flatMap(new CounterProgram[IO](_).program)
    // assert

    io.map { result =>
      assertEquals(result, 6)
    }
  }

}
