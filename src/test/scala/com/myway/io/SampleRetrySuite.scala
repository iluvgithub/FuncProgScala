package com.myway.io

import cats.effect._
import cats.effect.kernel.Ref
import com.myway.io.SampleRetry.checkoutWithRetry
import munit.CatsEffectSuite
import retry.RetryPolicies._
import retry._

import scala.concurrent.duration.DurationInt
class SampleRetrySuite extends CatsEffectSuite {

  test("checkoutWithRetry eventually succeeds after temporary failures") {

    def policy[F[_]: Temporal]: RetryPolicy[F] = limitRetries(5) join constantDelay(100.millis)

    for {
      ref    <- Ref.of[IO, Int](0)
      result <- checkoutWithRetry[IO](ref, policy)
      count  <- ref.get
    } yield {
      assertEquals(result, "ORDER_CONFIRMED")
      assertEquals(count, 3) // 2 failures + 1 success
    }
  }

  test("checkout fails if retries are exhausted") {

    def policy[F[_]: Temporal]: RetryPolicy[F] = limitRetries(5)

    for {
      ref    <- Ref.of[IO, Int](-10)
      result <- checkoutWithRetry[IO](ref, policy).attempt.map(_.fold(_.getMessage, identity))
      count  <- ref.get
    } yield {
      assertEquals(result, "Raise Error")
      assertEquals(count, -4) // 1 failure + 5 failed retries
    }
  }
}
