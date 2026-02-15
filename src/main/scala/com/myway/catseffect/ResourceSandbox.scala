package com.myway.catseffect

import cats.effect.{Resource, Sync}
import cats.syntax.all._
import fs2.{io, text, Stream}

import java.io._
object ResourceSandbox {

  val BUFFER_SIZE: Int = 1024 * 10

  def inputStream[F[_]: Sync](f: File): Resource[F, FileInputStream] =
    Resource.make {
      Sync[F].blocking(new FileInputStream(f)) // build
    } { inStream =>
      Sync[F]
        .blocking(inStream.close()) // release
        .handleErrorWith(_ => Sync[F].unit)
    }

  def outputStream[F[_]: Sync](f: File): Resource[F, FileOutputStream] =
    Resource.make {
      Sync[F].blocking(new FileOutputStream(f, true)) // build
    } { outStream =>
      Sync[F]
        .blocking(outStream.close()) // release
        .handleErrorWith(_ => Sync[F].unit)
    }

  def inputOutputStreams[F[_]: Sync](
    in: File,
    out: File
  ): Resource[F, (InputStream, OutputStream)] =
    for {
      inStream  <- inputStream[F](in)
      outStream <- outputStream[F](out)
    } yield (inStream, outStream)

  def transfer[F[_]: Sync](
    origin: InputStream,
    destination: OutputStream,
    buffer: Array[Byte],
    acc: Long
  ): F[Long] =
    for {

      amount <- Sync[F].blocking(origin.read(buffer, 0, buffer.length))
      count <-
        if (amount > -1)
          Sync[F].blocking(destination.write(buffer, 0, amount)) >> transfer(
            origin,
            destination,
            buffer,
            acc + amount
          )
        else
          Sync[F].pure(
            acc
          ) // End of read stream reached (by java.io.InputStream contract), nothing to write
    } yield count

  def deleteFile[F[_]: Sync](file: File): F[Unit] = Sync[F].blocking {
    if (file.exists()) file.delete()
  }.void

  def copy[F[_]: Sync](origin: File, destination: File): F[Long] =
    for {
      _ <- deleteFile(destination)
      length <- inputOutputStreams(origin, destination).use { case (in, out) =>
        transfer(in, out, new Array[Byte](BUFFER_SIZE), 0)
      }
    } yield length

  def inputStreamResource[F[_]: Sync](f: File): Resource[F, FileInputStream] =
    Resource.make {
      Sync[F].blocking(new FileInputStream(f))
    } { in =>
      Sync[F].blocking(in.close()).handleErrorWith(_ => Sync[F].unit)
    }
  def fileLines[F[_]: Sync](file: File): Stream[F, String] =
    Stream
      .resource(inputStreamResource(file))
      .flatMap { inStream =>
        io.readInputStream(
          Sync[F].pure(inStream: InputStream), // <-- fix
          chunkSize = BUFFER_SIZE,
          closeAfterUse = false
        )
      }
      .through(text.utf8.decode)
      .through(text.lines)
}
