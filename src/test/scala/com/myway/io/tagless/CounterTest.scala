package com.myway.io.tagless

import cats.effect.IO
import munit.CatsEffectSuite

class CounterTest extends CatsEffectSuite {

  test("increment and get on in memory counter") {
    // arrange
    def program(c: Counter[IO]): IO[Int] =
      for {
        _   <- c.get.flatMap(IO.println)
        _   <- c.increment
        _   <- c.get.flatMap(IO.println)
        _   <- c.increment.replicateA(5).void
        out <- c.get
        _   <- IO.println(out)
      } yield out
    // act
    val io: IO[Int] = Counter.make[IO].flatMap(program)
    // assert

    io.map { result =>
      assertEquals(result, 6)
    }
  }

}
