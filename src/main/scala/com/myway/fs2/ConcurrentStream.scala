package com.myway.fs2

import cats.effect.std.Console
import cats.effect.{Async, Temporal}
import cats.implicits.catsSyntaxFlatMapOps
import fs2.Stream

import scala.concurrent.duration.DurationInt
object ConcurrentStream {

  def streamBackground[F[_]: Async: Console]: Stream[F, Unit] =
    Stream.iterate[F, Int](0)(_ + 1).evalMap(x => Temporal[F].sleep(40.millis) >> Console[F].println("pulse"))

  def streamForeground[F[_]: Async: Console]: Stream[F, Int] =
    Stream
      .emits[F, Int](100 :: 200 :: 300 :: Nil)
      .evalMap(x => Temporal[F].sleep(100.millis) >> Console[F].println(s"x=${x}")>> Async[F].pure(x))
}
