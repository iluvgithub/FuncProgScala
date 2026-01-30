package com.myway.io
import cats.effect.IO
import munit.CatsEffectSuite

import scala.concurrent.duration._
class FirstEffectSuite extends CatsEffectSuite {
  test("IO.pure should immediately return the value") {
    val io: IO[Int] = IO.pure(42)

    io.map { result =>
      assertEquals(result, 42)
    }
  }

  test("IO with sleep should complete within timeout") {
    val io: IO[String] =
      IO.sleep(50.millis) >> IO.pure("awake")

    io.map { result =>
      assertEquals(result, "awake")
    }
  }

  test("Composed IO program should produce expected result") {
    val program: IO[Int] =
      for {
        a <- IO.pure(10)
        b <- IO.delay(32)
        _ <- IO.println("Running effectful computation")
      } yield a + b

    program.map { result =>
      assertEquals(result, 42)
    }
  }
}
