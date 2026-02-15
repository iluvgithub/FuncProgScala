package com.myway.io
import cats.effect.std.{Dispatcher, Queue}
import cats.effect.{Async, ExitCode, IO, IOApp}
import com.myway.io.CSVHandle.RowOrError
import fs2._

import java.io.{BufferedReader, File, FileReader}
import java.net.URL

trait CSVHandle {
  def withRows(cb: RowOrError => Unit): Unit
}

class FileCSVHandle(path: File) extends CSVHandle {
  override def withRows(cb: RowOrError => Unit): Unit = {
    val reader = new BufferedReader(new FileReader(path))
    try {
      var line: String = reader.readLine()
      while (line != null) {
        val row = line.split(",").toList
        cb(Right(row))
        line = reader.readLine()
      }
    } catch {
      case t: Throwable =>
        cb(Left(t))
    } finally
      reader.close()
  }
}

object CSVHandle extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {

    val url: URL = getClass.getClassLoader.getResource("sample/data.csv")
    val filePath: File = new File(url.getPath)
    val handle         = new FileCSVHandle(filePath)

    rows[IO](handle)
      .evalMap(row => IO.println(s"Row: $row"))
      .compile
      .drain
      .as(ExitCode.Success)

  }

  type Row        = List[String]
  type RowOrError = Either[Throwable, Row]

  def rows[F[_]](csvHandle: CSVHandle)(implicit F: Async[F]): Stream[F, Row] =
    for {
      dispatcher <- Stream.resource(Dispatcher.sequential[F])
      queue      <- Stream.eval(Queue.unbounded[F, Option[RowOrError]])
      _ <- Stream.eval {
        F.delay {
          def enqueue(v: Option[RowOrError]): Unit = dispatcher.unsafeRunAndForget(queue.offer(v))

          // Fill the data - withRows blocks while reading the file, asynchronously invoking the callback we pass to it on every row
          csvHandle.withRows(e => enqueue(Some(e)))
          // Upon returning from withRows, signal that our stream has ended.
          enqueue(None)
        }
      }
      // Due to `fromQueueNoneTerminated`, the stream will terminate when it encounters a `None` value
      row <- Stream.fromQueueNoneTerminated(queue).rethrow
    } yield row

}
