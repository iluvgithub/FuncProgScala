package com.myway.io
import cats.effect._
import cats.effect.testkit.TestControl
import cats.syntax.all._
import munit.CatsEffectSuite

import scala.concurrent.duration._
class SampleTestControlSuite extends CatsEffectSuite {

  def run[F[_]: Temporal](ref: Ref[F, List[Int]]): F[List[Int]] =
    for {
      _   <- ref.update(_ :+ 1)
      _   <- Temporal[F].sleep(1.second)
      _   <- ref.update(_ :+ 2)
      _   <- Temporal[F].sleep(1.second)
      _   <- ref.update(_ :+ 3)
      out <- ref.get
    } yield out

  test("RefProgram updates Ref state over time direct syntax") {
    TestControl.executeEmbed(for {
      ref   <- Ref.of[IO, List[Int]](List.empty)
      s0    <- ref.get
      fiber <- run(ref).start
      _     <- IO.sleep(100.millis)
      s1    <- ref.get

      _  <- IO.sleep(1.second)
      s2 <- ref.get

      _  <- fiber.join
      s3 <- ref.get
    } yield {
      assertEquals(s0, List())
      assertEquals(s1, List(1))
      assertEquals(s2, List(1, 2))
      assertEquals(s3, List(1, 2, 3))
    })
  }
}
