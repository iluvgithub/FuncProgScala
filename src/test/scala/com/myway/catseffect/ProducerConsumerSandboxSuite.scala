package com.myway.catseffect

import cats.effect.{IO, Ref}
import munit.CatsEffectSuite

import scala.collection.immutable.Queue
class ProducerConsumerSandboxSuite extends CatsEffectSuite {

  test("one producer, one consumer") {
    val maxi = 30000
    val io: IO[Option[Int]] =
      for {
        max    <- IO(maxi)
        queueR <- Ref.of[IO, Queue[Int]](Queue.empty[Int])
        _      <- ProducerConsumerSandbox.producer(queueR, 0, max)
        o      <- ProducerConsumerSandbox.consumer(queueR, max)
      } yield o

    io.map {
      case Some(i) => assertEquals(i, maxi)
      case None    => fail("should have a value")
    }

  }

}
