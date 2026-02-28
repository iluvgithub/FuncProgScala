package com.myway.fs2

import cats.effect.IO
import fs2.Stream
import munit.CatsEffectSuite
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
class ConcurrentStreamSuite extends CatsEffectSuite {

  test(" 2 concurrent streams") {
    // arrange
    val s1 = ConcurrentStream.streamForeground[IO]
    val s2= ConcurrentStream.streamBackground[IO]
    // act
    val out: Stream[IO, Int] = s1.concurrently(s2)
    // assert
    val actualsIo: IO[List[Int]] = out.compile.toList
    actualsIo.map(
      act => act shouldBe List(100, 200, 300)
    )
  }

}
