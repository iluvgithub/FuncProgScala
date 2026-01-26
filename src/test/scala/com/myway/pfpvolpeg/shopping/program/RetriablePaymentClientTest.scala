package com.myway.pfpvolpeg.shopping.program

import cats.Monad
import cats.effect.{IO, Ref}
import cats.implicits.{catsSyntaxEq => _, catsSyntaxFlatMapOps}
import com.myway.pfpvolpeg.shopping.domain.retry.Retry
import com.myway.pfpvolpeg.shopping.domain.{Payment, PaymentClient, PaymentError, PaymentId}
import munit.CatsEffectSuite
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import org.typelevel.log4cats.Logger
import retry.RetryPolicies.limitRetries
import retry.RetryPolicy
class RetriablePaymentClientTest extends CatsEffectSuite {

  val paymentClient: PaymentClient[IO] = mock[PaymentClient[IO]]

  test("policy 1 ") {
    // arrange
    implicit val lg: Logger[IO]  = SimpleConsoleLogger[IO]
    implicit val rlog: Retry[IO] = Retry.forLoggerTemporal[IO]
    val policy: RetryPolicy[IO]  = limitRetries[IO](3)
    val payment: Payment         = mock[Payment]
    when(paymentClient.process(payment))
      .thenReturn(IO.raiseError(new RuntimeException("boom 1")))
      .thenReturn(IO.raiseError(new RuntimeException("boom 2")))
      .thenReturn(IO.raiseError(new RuntimeException("boom 3")))
      .thenReturn(IO.raiseError(new RuntimeException("boom 4")))
      .thenReturn(IO.raiseError(new RuntimeException("boom 5")))
      .thenReturn(IO.pure(mock[PaymentId]))

    val paymentClient0: PaymentClient[IO] =
      (payment: Payment) => IO.defer(paymentClient.process(payment))

    val retriablePaymentClient = RetriablePaymentClient(paymentClient0, policy)
    // act
    val out: IO[PaymentId] = retriablePaymentClient.process(payment)
    // asset

    out.attempt.map(
      _.fold(
        t => {
          assert(t.isInstanceOf[PaymentError])
          val act = t.asInstanceOf[PaymentError]
          assertEquals(act.cause, "boom 4")
        },
        _ => fail("")
      )
    )

  }

  object SimpleConsoleLogger {
    import cats.effect.std.Console
    import org.typelevel.log4cats.Logger
    def apply[F[_]: Console: Monad]: Logger[F] = new Logger[F] {
      private def log(level: String, message: => String, useErrorStream: Boolean = false): F[Unit] =
        if (useErrorStream) Console[F].errorln(s"[$level] $message")
        else Console[F].println(s"[$level] $message")

      override def error(message: => String): F[Unit] = log("ERROR", message, useErrorStream = true)
      override def warn(message: => String): F[Unit]  = log("WARN", message, useErrorStream = true)
      override def info(message: => String): F[Unit]  = log("INFO", message)
      override def debug(message: => String): F[Unit] = log("DEBUG", message)
      override def trace(message: => String): F[Unit] = log("TRACE", message)

      override def error(t: Throwable)(message: => String): F[Unit] =
        log("ERROR", message, useErrorStream = true) >> Console[F].printStackTrace(t)
      override def warn(t: Throwable)(message: => String): F[Unit] =
        log("WARN", message, useErrorStream = true) >> Console[F].printStackTrace(t)
      override def info(t: Throwable)(message: => String): F[Unit] =
        log("INFO", message) >> Console[F].printStackTrace(t)
      override def debug(t: Throwable)(message: => String): F[Unit] =
        log("DEBUG", message) >> Console[F].printStackTrace(t)
      override def trace(t: Throwable)(message: => String): F[Unit] =
        log("TRACE", message) >> Console[F].printStackTrace(t)
    }
  }

}
