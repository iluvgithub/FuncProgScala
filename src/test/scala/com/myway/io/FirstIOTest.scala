package com.myway.io

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._

class FirstIOTest extends AnyFunSuite with Matchers {
  test("IO.pure should immediately return the value") {
    val io: IO[Int] = IO.pure(42)

    val result = io.unsafeRunSync()

    result shouldBe 42
  }
  test("IO with sleep should complete within timeout (using unsafeRunTimed for safety)") {
    val io: IO[String] = IO.sleep(500.millis) >> IO.pure("awake")
    val result = io.unsafeRunTimed(2.seconds)
    result shouldBe Some("awake")
  }
  test("Composed IO program should produce expected result") {
    val program: IO[Int] =
      for {
        a <- IO.pure(10)
        b <- IO.delay(32)
        _ <- IO.println("Running effectful computation")
      } yield a + b

    val result = program.unsafeRunSync()

    result shouldBe 42
  }
}
