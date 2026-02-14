package com.myway.io

import cats.effect.IO
import com.myway.io.CSVHandle.{rows, Row, RowOrError}
import fs2._
import munit.CatsEffectSuite

class CSVHandleSuite extends CatsEffectSuite {

  class DummyCSVHandle(data: List[String]) extends CSVHandle {
    override def withRows(cb: RowOrError => Unit): Unit =
      data.foreach { line =>
        val row = line.split(",").toList
        cb(Right(row))
      }
  }

  test("dummy file") {
    // arrange
    val csvLines = List(
      "abc,def",
      "x12,y456"
    )
    val handle                  = new DummyCSVHandle(csvLines)
    val stream: Stream[IO, Row] = rows[IO](handle)
    // act
    val listIo: IO[List[Row]] = stream.compile.toList
    // assert

    val expected = List(
      List("abc", "def"),
      List("x12", "y456")
    )
    listIo.map(result => assertEquals(expected, result))

  }

}
