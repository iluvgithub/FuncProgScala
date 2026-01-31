package com.myway.io

import cats.effect._
import cats.effect.std.Queue
import cats.effect.testkit.TestControl
import munit.CatsEffectSuite

import scala.concurrent.duration.DurationInt
// https://medium.com/@fqaiser94/using-testcontrol-to-test-fs2-streams-92cec1fac217

class Fs2StreamParallelQueueDrainSuite extends CatsEffectSuite {
  test("drains queue in parallel and shows state changes over time") {

    val maxConcurrent = 2
    val queueSize     = 7
    val program = for {
      queue     <- Queue.unbounded[IO, Int]
      processed <- Ref.of[IO, List[Int]](List.empty)

      _ <- List.range(0, queueSize).foldLeft(IO(()))((o, n) => o >> queue.offer(n))

      fiber <- Fs2StreamParallelQueueDrain
        .drainQueue[IO,Int](maxConcurrent, queue, processed, _ <= 4)
        .compile
        .drain
        .start

      s0 <- processed.get

      _  <- IO.sleep(1.second)
      s1 <- processed.get

      _  <- IO.sleep(1.second)
      s2 <- processed.get

      _  <- IO.sleep(1.second)
      s3 <- processed.get

    //  _ <- fiber.cancel
    } yield (s0, s1, s2, s3)

    TestControl.executeEmbed(program).map { case (s0, s1, s2, s3) =>
      assertEquals(s0, List.empty, "should be empty")
      assertEquals(s1.size, 2, "should have processed some elements")
      assertEquals(s1.toSet, Set(0, 1), "should have processed some elements")
      assertEquals(s2.size, 4, "should have processed more elements")
      assertEquals(s2.toSet, Set(0, 1, 2, 3), "should have processed more elements")
      assertEquals(s3.size, 5, "should have processed all elements")
      assertEquals(s3.toSet, Set(0, 1, 2, 3, 4), "should have processed all elements")
    }
  }

}
