package com.myway.io.tagless

import cats.Apply
import cats.effect.kernel.Ref
import cats.implicits.{catsSyntaxApplyOps, toFunctorOps}
trait Items[F[_]] {
  def add(item: Item): F[Unit]
}
trait Item

object Items {
  import cats.Functor
  def make[F[_]: Functor: Ref.Make]: F[Items[F]] =
    Ref.of[F, List[Item]](Nil).map { ref =>
      new Items[F] {
        override def add(item: Item): F[Unit] = ref.update(l => item :: l)
      }
    }
}

class ItemsCounter[F[_]: Apply](
  counter: Counter[F],
  items: Items[F]
) {
  def addItem(item: Item): F[Unit] =
    items.add(item) *> counter.increment

  def countItems: F[Int] = counter.get
}
