package com.myway.io
import cats.effect.{IO, Ref}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import weaver.SimpleIOSuite

import scala.concurrent.duration.DurationInt
object SampleRefUpdaterWeaverSuite extends SimpleIOSuite {

  test("SimpleRefUpdater appends 1,2,3 in order") {
    for {
      ref    <- Ref.of[IO, List[Int]](Nil)
      result <- SimpleRefUpdater.run[IO](ref)
    } yield expect(result == List(1, 2, 3))
  }

  test("SimpleRefUpdater appends 1,2,3 in order + resource") {
    val mockRelease = mock[List[Int] => IO[Unit]]
    when(mockRelease.apply(any())).thenReturn(IO(()))
    for {
      ref    <- Ref.of[IO, List[Int]](Nil)
      result <- SimpleRefUpdater.runInResource[IO](ref, mockRelease).use(IO(_))
      _      <- IO(org.mockito.MockitoSugar.verify(mockRelease).apply(any()))
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
    } yield expect.all(
      check == List(1, 2),
      finalList == List(1, 2, 3)
    )

  }
}
