package com.myway.fs2
import cats.effect.IO
import fs2.Stream
import munit.CatsEffectSuite
class TwoStreamsMatcherSuite extends CatsEffectSuite {

  test(" emit in sorted order case 1") {
    // arrange
    val stream1: Stream[IO, String] = Stream.emits(List("a", "b", "c", "d", "f"))
    val stream2: Stream[IO, String] = Stream.emits(List("a", "c", "d", "e", "f"))
    // act
    val outIo: IO[List[String]] =
      TwoStreamsMatcher
        .mergeSorted[IO, String](stream1, stream2, s1 => s2 => s1.compareTo(s2))
        .compile
        .toList
    // assert
    outIo.map(out => assertEquals(out, List("a", "c", "d", "f")))
  }

  test(" emit in sorted order case 2") {
    // arrange
    val stream1: Stream[IO, Int] = Stream.emits(List(0, 2, 5, 6))
    val stream2: Stream[IO, Int] = Stream.emits(List(-1, 1, 2, 3, 4, 5, 7))
    // act
    val outIoA: IO[List[Int]] =
      TwoStreamsMatcher
        .mergeSorted[IO, Int](stream1, stream2, s1 => s2 => s1.compareTo(s2))
        .compile
        .toList
    val outIoB: IO[List[Int]] =
      TwoStreamsMatcher
        .mergeSorted[IO, Int](stream1, stream2, s1 => s2 => s1.compareTo(s2))
        .compile
        .toList
    // assert
    outIoA.map(out => assertEquals(out, List(2, 5)))
    outIoB.map(out => assertEquals(out, List(2, 5)))
  }

  test(" merge combine") {
    // arrange
    val infiniteInts: Stream[IO, Int] = Stream.iterate(0)(_ + 1)
    case class Foo(key: String, value: Int) {
      def merge(that: Foo) = Foo(key, this.value + that.value)
    }
    val stream1: Stream[IO, Foo] =
      Stream.emits(List("a", "b", "c", "d", "f")).zip(infiniteInts).map { case (k, v) => Foo(k, v) }
    val stream2: Stream[IO, Foo] =
      Stream.emits(List("a", "c", "d", "e", "f")).zip(infiniteInts).map { case (k, v) => Foo(k, v) }

    // act
    val outIo: IO[List[Foo]] =
      TwoStreamsMatcher
        .merge[IO, String, Foo](
          stream1,
          stream2,
          getter = _.key,
          s1 => s2 => s1.compareTo(s2),
          f1 => f2 => f1.merge(f2)
        )
        .compile
        .toList
    // assert
    outIo.map(out =>
      assertEquals(
        out,
        List(
          Foo("a", 0),
          Foo("c", 3),
          Foo("d", 5),
          Foo("f", 8)
        )
      )
    )
  }
}
