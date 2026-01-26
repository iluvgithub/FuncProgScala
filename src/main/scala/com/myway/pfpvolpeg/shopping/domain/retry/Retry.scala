package com.myway.pfpvolpeg.shopping.domain.retry

import cats.Show
import cats.effect.Temporal
import cats.syntax.all._
import org.typelevel.log4cats.Logger
import retry.RetryDetails.{GivingUp, WillDelayAndRetry}
import retry.{retryingOnAllErrors, RetryDetails, RetryPolicy}
trait Retry[F[_]] {
  def retry[A](policy: RetryPolicy[F], retriable: Retriable)(fa: F[A]): F[A]
}

trait Retriable

object Retriable {
  case object Orders   extends Retriable
  case object Payments extends Retriable
}

object Retry {
  def apply[F[_]: Retry]: Retry[F] = implicitly

  implicit val showRetriable: Show[Retriable] = Show.show { retriable =>
    s"${retriable}"
  }

  implicit def forLoggerTemporal[F[_]: Logger: Temporal]: Retry[F] =
    new Retry[F] {
      def retry[A](
        policy: RetryPolicy[F],
        retriable: Retriable
      )(fa: F[A]): F[A] = {
        def onError(
          e: Throwable,
          details: RetryDetails
        ): F[Unit] =
          details match {
            case WillDelayAndRetry(_, retriesSoFar, _) =>
              Logger[F].error(
                s"Failed on ${retriable.show}. We retried $retriesSoFar times. error was:${e.getMessage}"
              )
            case GivingUp(totalRetries, _) =>
              Logger[F].error(
                s"Giving up on ${retriable.show} after $totalRetries retries."
              )
          }
        retryingOnAllErrors[A](policy, onError)(fa)
      }
    }
}
