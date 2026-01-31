package com.myway.io

import cats.MonadThrow
import cats.effect._
import cats.effect.kernel.Ref
import cats.syntax.all._
import retry._

object SampleRetry {

  // Your original logic (simulates: fail twice → succeed on 3rd attempt)
  def checkout[F[_]: Temporal](
    attempt: Ref[F, Int]
  ): F[String] = {
    val out: F[Either[Throwable, String]] = attempt
      .modify { n =>
        if (n < 2)
          (n + 1, Left(new RuntimeException("Raise Error")))
        else
          (n + 1, Right("ORDER_CONFIRMED"))
      }
    out.flatMap {
      case Left(e)  => MonadThrow[F].raiseError(e)
      case Right(v) => v.pure[F]
    }
  }

  def checkoutWithRetry[F[_]: Temporal](
    attempt: Ref[F, Int],
    policy: RetryPolicy[F]
  ): F[String] = {

    val isWorthRetrying: Throwable => F[Boolean] = _ => Temporal[F].pure(true)

    retryingOnSomeErrors[String](
      policy = policy,
      isWorthRetrying,
      onError = (err: Throwable, details: RetryDetails) =>
        // Optional: logging, metrics, etc.
        // println(s"Retry attempt #${details.retriesSoFar} after error: $err")
        ().pure[F]
    ) {
      checkout(attempt)
    }
  }

}
