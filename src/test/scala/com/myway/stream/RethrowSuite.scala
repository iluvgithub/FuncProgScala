package com.myway.stream
import cats.effect.IO
import cats.implicits._
import fs2.{Pure, Stream}
import munit.CatsEffectSuite

class RethrowSuite extends CatsEffectSuite {

  test("rethrow succeeds when all values are Right") {
    val stream: Stream[IO, Int] =
      Stream
        .emits(List[Either[Throwable, Int]](
          Right(1),
          Right(2),
          Right(3)
        ))
        .covary[IO]
        .rethrow

    stream.compile.toList.map { result =>
      assertEquals(result, List(1, 2, 3))
    }
  }

  test("rethrow fails when a Left appears") {
    val boom = new RuntimeException("boom")

    val stream: Stream[IO, Int] =
      Stream
        .emits(List[Either[Throwable, Int]](
          Right(1),
          Left(boom),
          Right(3) // never reached
        ))
        .covary[IO]
        .rethrow

    stream.compile.toList.attempt.map {
      case Left(e)  => assertEquals(e.getMessage, "boom")
      case Right(_) => fail("Stream was expected to fail but succeeded")
    }
  }
}
