package com.myway.pfpvolpeg.shopping.program

import cats.MonadThrow
import cats.implicits.catsSyntaxMonadError
import com.myway.pfpvolpeg.shopping.domain.retry.{Retriable, Retry}
import com.myway.pfpvolpeg.shopping.domain.{Payment, PaymentClient, PaymentError, PaymentId}
import retry.RetryPolicy
case class RetriablePaymentClient[F[_]: MonadThrow: Retry](
  paymentClient: PaymentClient[F],
  policy: RetryPolicy[F]
) extends PaymentClient[F] {

  override def process(payment: Payment): F[PaymentId] = processPayment(payment)

  private def processPayment(in: Payment): F[PaymentId] =
    Retry[F]
      .retry(policy, Retriable.Payments)(paymentClient.process(in))
      .adaptError { case e =>
        PaymentError(Option(e.getMessage).getOrElse("Unknown"))
      }
}
