package com.myway.io

import cats.effect.{Ref, Resource, Temporal}
import cats.syntax.all._

import scala.concurrent.duration._
object SimpleRefUpdater {

  def run[F[_]: Temporal](ref: Ref[F, List[Int]]): F[List[Int]] =
    for {
      _   <- ref.update(_ :+ 1)
      _   <- Temporal[F].sleep(1.second)
      _   <- ref.update(_ :+ 2)
      _   <- Temporal[F].sleep(1.second)
      _   <- ref.update(_ :+ 3)
      out <- ref.get
    } yield out

  def runInResource[F[_]: Temporal](
    ref: Ref[F, List[Int]],
    release: List[Int] => F[Unit]
  ): Resource[F, List[Int]] = Resource.make(run(ref))(release)
}
