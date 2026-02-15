package com.myway.catseffect

import cats.effect.{Deferred, IO, Ref}
import com.myway.catseffect.ProducerConsumerAsync.{consumer, producer, QueueState}
import munit.CatsEffectSuite

import scala.collection.immutable.Queue

class ProducerConsumerAsyncSuite extends CatsEffectSuite {

  test("one producer, one consumer") {
    val maxi = 200000
    val nb   = 5
    val io: IO[List[Int]] = for {
      stateR <- Ref.of[IO, QueueState[IO, Int]](
        QueueState(Queue.empty[Int], Queue.empty[Deferred[IO, Int]], maxi)
      )
      counterR <- Ref.of[IO, Int](1)
      producers = List.range(0, nb).map(producer(_, counterR, stateR))
      consumers = List.range(0, nb).map(consumer(_, stateR))
      output <-
        (producers ++ consumers).parSequence // Run producers and consumers in parallel until done (likely by user cancelling with CTRL-C)
          .handleErrorWith { t =>
            cats.effect.std.Console[IO].errorln(s"Error caught: ${t.getMessage}").as(List())
          }
    } yield output

    io.map(actual => assertEquals(actual.max, maxi + nb - 1))

  }

}
