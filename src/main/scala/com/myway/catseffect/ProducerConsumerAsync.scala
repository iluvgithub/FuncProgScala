package com.myway.catseffect
import cats.effect.std.Console
import cats.effect.{Async, Deferred, Ref}
import cats.syntax.all._

import scala.collection.immutable.Queue
import scala.concurrent.duration.DurationInt
object ProducerConsumerAsync {
  case class QueueState[F[_], A](queue: Queue[A], takers: Queue[Deferred[F, A]], max: Int) {}

  def producer[F[_]: Async: Console](
    id: Int,
    counterR: Ref[F, Int],
    stateR: Ref[F, QueueState[F, Int]]
  ): F[Int] = {

    def offer(i: Int): F[Unit] =
      stateR.modify {
        case QueueState(queue, takers, n) if takers.nonEmpty =>
          val (taker, rest) = takers.dequeue
          QueueState(queue, rest, n) -> taker.complete(i).void
        case QueueState(queue, takers, n) =>
          QueueState(queue.enqueue(i), takers, n) -> Async[F].unit
      }.flatten

    for {
      i <- counterR.getAndUpdate(_ + 1)
      _ <- offer(i)
      _ <- Async[F].whenA(i % 100000 == 0)(Console[F].println(s"Producer $id has reached $i items"))
      _ <- Async[F].sleep(1.microsecond) // To prevent overwhelming consumers
      mx <- stateR.get.map(_.max)
      o  <- if (i < mx) producer(id, counterR, stateR) else Async[F].pure(i)
    } yield o

  }

  def consumer[F[_]: Async: Console](
    id: Int,
    stateR: Ref[F, QueueState[F, Int]]
  ): F[Int] = {
    val take: F[Int] =
      Deferred[F, Int].flatMap { taker =>
        for {
          foutcome <- stateR.modify {
            case QueueState(queue, takers, n) if queue.nonEmpty =>
              val (i, rest) = queue.dequeue
              QueueState(rest, takers, n) -> Async[F].pure(
                i
              ) // Got element in queue, we can just return it
            case QueueState(queue, takers, n) =>
              QueueState(
                queue,
                takers.enqueue(taker),
                n
              ) -> taker.get // No element in queue, must block caller until some is available
          }
          flatten <- foutcome
        } yield flatten
      }

    for {
      i <- take
      _ <- Async[F].whenA(i % 10000 == 0)(Console[F].println(s"Consumer $id has reached $i items"))

      mx <- stateR.get.map(_.max)
      o  <- if (i < mx) consumer(id, stateR) else Async[F].pure(i)
    } yield o
  }
}
