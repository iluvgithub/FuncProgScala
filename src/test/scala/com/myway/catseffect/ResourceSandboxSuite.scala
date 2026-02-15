package com.myway.catseffect

import cats.effect.IO
import munit.CatsEffectSuite

import java.io.File

class ResourceSandboxSuite extends CatsEffectSuite {
  val path: String = getClass.getClassLoader.getResource("csv").getPath

  test("file found") {
    // arrange
    val inFile: File  = new File(s"$path/data.csv")
    val outFile: File = new File(s"$path/datacopy.csv")
    // act
    val outIo: IO[Long] = ResourceSandbox.copy[IO](inFile, outFile)

    // assert
    val expectedSize = 38L
    outIo.map(out => assertEquals(out, expectedSize))
  }

  test("file to stream") {
    // arrange
    val inFile: File                    = new File(s"$path/data.csv")
    val linesIO: fs2.Stream[IO, String] = ResourceSandbox.fileLines[IO](inFile)
    // act
    val outIo: IO[List[String]] = linesIO.compile.toList
    // assert
    outIo.map(out =>
      assertEquals(
        List(
          "col1,col2,col3",
          "a11,a12,a13",
          "a21,a22,a23"
        ),
        out
      )
    )
  }
}
