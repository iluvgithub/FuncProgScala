package com.myway.io
import cats.effect._
import cats.effect.std.Queue
import cats.syntax.apply._
import fs2.Stream

import scala.concurrent.duration._

object Fs2StreamParallelQueueDrain {

  def drainQueue[F[_]: Temporal](
    maxConcurrent: Int,
    queue: Queue[F, Int],
    processed: Ref[F, List[Int]]
  ): Stream[F, Unit] =
    Stream
      .fromQueueUnterminated(queue)
      .parEvalMap(maxConcurrent) { elem =>
        Temporal[F].sleep(900.millis) *> processed.update(_ :+ elem)
      }
}
