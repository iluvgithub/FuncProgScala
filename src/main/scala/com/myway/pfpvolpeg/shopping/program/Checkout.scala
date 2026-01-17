package com.myway.pfpvolpeg.shopping.program

import cats.MonadThrow
import cats.data.NonEmptyList
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.myway.pfpvolpeg.shopping.domain._

final case class Checkout[F[_] : MonadThrow](
                                              payments: PaymentClient[F],
                                              cart: ShoppingCart[F],
                                              orders: Orders[F]
                                            ) {
  private def ensureNonEmpty[A](xs: List[A]): F[NonEmptyList[A]] =
    MonadThrow[F].fromOption(
      NonEmptyList.fromList(xs),
      EmptyCartError
    )

  def process(userId: UserId, card: Card): F[OrderId] =
    for {
      c <- cart.get(userId)
      its <- ensureNonEmpty(c.items)
      pid <- payments.process(Payment(userId, c.total, card))
      oid <- orders.create(userId, pid, its, c.total)
      _ <- cart.delete(userId)
    } yield oid
}