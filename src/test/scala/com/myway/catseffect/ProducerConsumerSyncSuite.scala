package com.myway.catseffect

import cats.effect.{IO, Ref}
import munit.CatsEffectSuite

import scala.collection.immutable.Queue
class ProducerConsumerSyncSuite extends CatsEffectSuite {

  test("one producer, one consumer") {
    val maxi = 50000
    val io: IO[Option[Int]] =
      for {
        max    <- IO(maxi)
        queueR <- Ref.of[IO, Queue[Int]](Queue.empty[Int])
        _      <- ProducerConsumerSync.producer(queueR, 0, max)
        o      <- ProducerConsumerSync.consumer(queueR, max)
      } yield o

    io.map {
      case Some(i) => assertEquals(i, maxi)
      case None    => fail("should have a value")
    }

  }

}
