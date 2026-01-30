package com.myway.io
import cats.effect._
import cats.effect.testkit.TestControl
import munit.CatsEffectSuite
import scala.concurrent.duration._

class SampleTestControlSuite extends CatsEffectSuite {


  def program(ref: Ref[IO, Vector[String]]): IO[Unit] =
    IO.pure("start") >>
      ref.update(_ :+ "start") >>
      IO.sleep(1.second) >>
      IO.pure("mid") >>
      ref.update(_ :+ "mid") >>
      IO.sleep(1.second) >>
      IO.pure("end") >>
      ref.update(_ :+ "end")

  test("observe intermediary states over virtual time") {

    val testIO =
      for {
        ref <- Ref.of[IO, Vector[String]](Vector.empty)
        _   <- program(ref).start
        _   <- IO.sleep(0.seconds) // allow initial sync effects
        s0  <- ref.get
        _   <- IO.sleep(1.second)
        s1  <- ref.get
        _   <- IO.sleep(1.second)
        s2  <- ref.get
      } yield (s0, s1, s2)

    TestControl.executeEmbed(testIO).map {
      case (s0, s1, s2) =>
        assertEquals(s0, Vector("start"))
        assertEquals(s1, Vector("start", "mid"))
        assertEquals(s2, Vector("start", "mid", "end"))
    }
  }
}