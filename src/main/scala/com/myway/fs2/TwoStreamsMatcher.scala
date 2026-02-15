package com.myway.fs2

import cats.Monad
import fs2.io.file.Flag
import fs2.{Pull, Stream}

import scala.collection.View.FlatMap

case object TwoStreamsMatcher {

  def mergeSorted[F[_]: Monad, A](
    streamLeft: Stream[F, A],
    streamRight: Stream[F, A],
    comparator: A => A => Int
  ): Stream[F, A] = merge(
    streamLeft,
    streamRight,
    (a: A) => a,
    comparator,
    (a1: A) => (_: A) => Monad[F].pure(a1)
  )
  def merge[F[_], A, B](
    streamLeft: Stream[F, B],
    streamRight: Stream[F, B],
    getter: B => A,
    comparator: A => A => Int,
    combine: B => B => F[B]
  ): Stream[F, B] = {
    def go(
      s1: Stream[F, B],
      s2: Stream[F, B]
    ): Pull[F, B, Unit] =
      s1.pull.uncons1.flatMap {
        case Some((h1, t1)) =>
          s2.pull.uncons1.flatMap {
            case Some((h2, t2)) =>
              val cmp = comparator(getter(h1))(getter(h2))


              if (cmp.equals(0)) Pull.eval(combine(h1)(h2)).flatMap(Pull.output1)  >> go(t1, t2)
              else if (cmp < 0) {
                go(t1, s2)
              } else {
                go(s1, t2)
              }

            case None => // s2 is empty
              Pull.done
          }
        case None =>
          Pull.done

      }

    go(streamLeft, streamRight).stream
  }

}
