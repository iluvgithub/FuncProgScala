package com.myway.io
import cats.effect.{IO, Ref}
import weaver.SimpleIOSuite

import scala.concurrent.duration.DurationInt
object SampleRefUpdaterWeaverSuite extends SimpleIOSuite {

  test("SimpleRefUpdater appends 1,2,3 in order") {
    for {
      ref    <- Ref.of[IO, List[Int]](Nil)
      result <- SimpleRefUpdater.run[IO](ref)
    } yield expect(result == List(1, 2, 3))

  }
  test("should take at least ~2 seconds") {
    for {
      ref   <- Ref.of[IO, List[Int]](Nil)
      start <- IO.realTime
      fiber <- SimpleRefUpdater.run[IO](ref).start
      _     <- IO.sleep(1900.millis) // a bit less than 2s
      check <- ref.get
      _     <- fiber.join            // make sure it completed
      end   <- IO.realTime
      duration = end - start
      _         <- IO(expect(duration >= 1900.millis))
      finalList <- ref.get
    } yield {
      expect(check == List())
      expect(finalList == List(1, 2, 3))
    }
  }
}
