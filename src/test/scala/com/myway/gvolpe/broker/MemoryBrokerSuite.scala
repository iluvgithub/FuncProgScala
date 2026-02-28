package com.myway.gvolpe.broker
import cats.effect.IO
import cats.effect.std.Queue
import fs2.Stream
import munit.CatsEffectSuite
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import scala.concurrent.duration.DurationInt
class MemoryBrokerSuite extends CatsEffectSuite {

  test(" 2 concurrent streams with foreground consumer and background producer") {
    // arrange
    val n = 10
    val io: IO[Stream[IO, Int]] = for {
      queue <- Queue.bounded[IO, Option[Int]](capacity = 5)
      producerResource = MemoryBroker.localProducer(queue)
      p = Stream
        .resource[IO, Producer[IO, Int]](producerResource)
        .flatMap(p => Stream.range(0, n).evalMap(i => p.send(i)))
      c   = MemoryBroker.localConsumer[IO, Int](queue).receive.metered(10.millis)
      out = c.concurrently(p)
    } yield out

    // act
    val ioList: IO[List[Int]] = io.flatMap(_.compile.toList)
    // assert
    ioList.map(act => act shouldBe List.range(0, n))
  }

}
