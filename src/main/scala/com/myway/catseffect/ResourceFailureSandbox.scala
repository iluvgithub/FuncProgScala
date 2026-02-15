package com.myway.catseffect
import cats.effect.{IO, IOApp, Resource}
object ResourceFailureSandbox extends IOApp.Simple {

  val myResource: Resource[IO, String] =
    Resource.make {
      IO.println("acquire resource") *> IO.pure("hello")
    } { _ =>
      IO.println("release resource")
    }

  val program: IO[Unit] =
    myResource.use { value =>
      IO.println(s"using: $value") *>
        IO.raiseError(new RuntimeException("boom!")) // <-- failure
    }

  override def run: IO[Unit] =
    program.handleErrorWith(e => IO.println(s"caught: ${e.getMessage}"))
}
