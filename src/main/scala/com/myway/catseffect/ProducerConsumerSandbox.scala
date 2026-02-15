package com.myway.catseffect
import cats.effect._
import cats.effect.std.Console
import cats.syntax.all._

import scala.collection.immutable.Queue

object ProducerConsumerSandbox {

  def producer[F[_]: Sync: Console](queueR: Ref[F, Queue[Int]], counter: Int, max: Int): F[Unit] =
    for {
      _ <- Sync[F].whenA(counter % 10000 == 0)(Console[F].println(s"Produced $counter items"))
      _ <- queueR.getAndUpdate(_.enqueue(counter + 1))
      _ <- if (counter < max) producer(queueR, counter + 1, max) else Sync[F].pure(())
    } yield ()

  def consumer[F[_]: Sync: Console](queueR: Ref[F, Queue[Int]], max: Int): F[Option[Int]] =
    for {
      iO <- queueR.modify { queue =>
        queue.dequeueOption.fold((queue, Option.empty[Int])) { case (i, queue) =>
          (queue, Option(i))
        }
      }
      _ <- Sync[F].whenA(iO.exists(_ % 10000 == 0))(Console[F].println(s"Consumed ${iO.get} items"))
      out <- if(iO.getOrElse(-1).equals(max)) Sync[F].pure(iO) else consumer(queueR, max)
    } yield out

}
