package com.myway.gvolpe.broker
import cats.effect.IO
import cats.effect.std.Queue
import fs2.Stream
import munit.CatsEffectSuite
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
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
      c   = MemoryBroker.localConsumer[IO, Int](queue).receive
      out = c.concurrently(p)
    } yield out

    // act
    val ioList: IO[List[Int]] = io.flatMap(_.compile.toList)
    // assert
    ioList.map(act => act shouldBe List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9))
  }

}
