package com.myway.io
import cats.effect._
import cats.effect.std.Queue
import fs2.Stream

import scala.concurrent.duration._

object Fs2StreamParallelQueueDrain {

  def drainQueue(
    queue: Queue[IO, Int],
    processed: Ref[IO, List[Int]]
  ): Stream[IO, Unit] =
    Stream
      .fromQueueUnterminated(queue)
      .parEvalMap(2) { elem => // parallelism = 2
        IO.sleep(900.millis) *>
          processed.update(_ :+ elem)
      }
}
