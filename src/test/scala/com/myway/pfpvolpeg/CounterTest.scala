package com.myway.pfpvolpeg

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class CounterTest extends AnyFunSuite with Matchers {

  test("in memory counter") {
    val counter: IO[Counter[IO]] = Counter.make[IO]
    val next: IO[Int] = for {
      k <- counter
      _ <- k.incr
      _ <- k.incr
      out <- k.get
      _ <- k.incr
    } yield out

    val result = next.unsafeRunSync()

    result shouldBe 2
  }

}
