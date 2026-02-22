package com.myway.io.tagless

import cats.effect.kernel.Ref
import cats.implicits.toFunctorOps

trait Counter[F[_]] {
  def increment: F[Unit]
  def get: F[Int]
}

object Counter {
  import cats.Functor
  def make[F[_]: Functor: Ref.Make]: F[Counter[F]] =
    Ref.of[F, Int](0).map { ref =>
      new Counter[F] {
        def increment: F[Unit] = ref.update(_ + 1)
        def get: F[Int]        = ref.get
      }
    }
}
