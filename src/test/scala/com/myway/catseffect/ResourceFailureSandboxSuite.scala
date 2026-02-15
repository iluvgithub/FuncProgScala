package com.myway.catseffect

import cats.effect.{IO, Resource}
import munit.CatsEffectSuite
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, times, verify, when}
import org.mockito.MockitoSugar.mock

class ResourceFailureSandboxSuite extends CatsEffectSuite {

  trait Releaser {
    def release(s: String): IO[Unit]
  }

  test("success on acquire") {
    // arrange
    val mockCallback: Releaser = mock[Releaser]
    when(mockCallback.release(any())).thenReturn(IO(()))
    val toAquire: IO[String] = IO.println("acquire") *> IO("xy")
    val goodResource: Resource[IO, String] =
      Resource.make(toAquire)(s => IO.println("release resource") *> mockCallback.release(s))
    // act
    val ioString: IO[String] = goodResource.use(IO(_))
    // assert
    ioString.attempt.map {
      case Left(_) => fail("failed")
      case Right(u) =>
        assertEquals(u, "xy")
        verify(mockCallback, times(1)).release(any())
    }
  }

  test("failure on acquire") {
    // arrange
    val mockCallback: Releaser = mock[Releaser]
    val toAquire: IO[String] =
      IO.println("acquire") *> IO.raiseError[String](new Exception("fail acquire"))

    val badResource: Resource[IO, String] =
      Resource.make(toAquire)(s => IO.println("release resource") *> mockCallback.release(s))
    // act
    val ioString: IO[String] = badResource.use(IO(_))
    // assert
    ioString.attempt.map {
      case Left(e) =>
        assertEquals(e.getMessage, "fail acquire")
        verify(mockCallback, never).release(any())
      case Right(_) => fail("failed")
    }

  }

  test("failure on use") {
    // arrange
    val mockCallback: Releaser = mock[Releaser]
    when(mockCallback.release(any())).thenReturn(IO(()))
    val toAquire: IO[String] = IO.println("acquire") *> IO("xy")
    val badResource: Resource[IO, String] =
      Resource.make(toAquire)(s => IO.println("release resource") *> mockCallback.release(s))
    // act
    val ioString: IO[String] =
      badResource.use(_ => IO.raiseError[String](new Exception("fail acquire")))
    // assert
    ioString.attempt.map {
      case Left(e) =>
        assertEquals(e.getMessage, "fail acquire")
        verify(mockCallback, times(1)).release(any())
      case Right(_) =>
        fail("failed")
    }

  }

  test("failure on release") {
    // arrange
    val mockCallback: Releaser = mock[Releaser]
    when(mockCallback.release(any()))
      // .thenReturn(IO(()))
      .thenReturn(IO.raiseError(new RuntimeException("cannot close")))
    val toAquire: IO[String] = IO.println("acquire") *> IO("xy")
    val badResource: Resource[IO, String] =
      Resource.make(toAquire)(s => IO.println("release resource") *> mockCallback.release(s))
    // act
    val ioString: IO[String] = badResource.use(IO(_))
    // assert
    ioString.attempt.map {
      case Left(e) =>
        verify(mockCallback, times(1)).release(any())
        assertEquals(e.getMessage, "cannot close")
      case Right(_) =>
        fail("failed")
    }

  }
}
