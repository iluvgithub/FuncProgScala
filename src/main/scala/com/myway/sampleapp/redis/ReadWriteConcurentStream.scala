package com.myway.sampleapp.redis
import cats.effect.{ExitCode, IO, IOApp}
import com.myway.sampleapp.config.AppConfigLoader
import com.myway.sampleapp.redis.ReadWriteConcurentStream.runFrom
import fs2.Stream
import org.slf4j
import org.slf4j.LoggerFactory
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object ReadWriteConcurentStream extends IOApp {

  implicit val logger: Logger[IO]   = Slf4jLogger.getLogger[IO]
  implicit val logErr: slf4j.Logger = LoggerFactory.getLogger(getClass)

  override def run(args: List[String]): IO[ExitCode] = runFrom(Some(0))

  def runFrom(optInit: Option[Int]): IO[ExitCode] = {
    val readWriterIo: IO[RedisReadWrite[IO]] = for {
      appConfig <- AppConfigLoader.loadConfig[IO]()
    } yield RedisReadWrite.makeRedisReadWrite(appConfig.redisConfig)

    def oneStep: IO[String] = for {
      readWrite <- readWriterIo
      n         <- readWrite.read("key").map(_.getOrElse("0"))
      _         <- readWrite.write("key", s"${n.toInt + 1}")
    } yield n
    val nbStep = 100

    val stream: Stream[IO, String] =
      Stream.eval(optInit match {
        case Some(value) => readWriterIo.flatMap(_.write("key", s"$value"))
        case None        => IO("")
      }) >> Stream.repeatEval(oneStep).take(nbStep)
    val o = stream
      .evalMap { value =>
        logger.info(s"Incremented to: $value")
      }
      .compile
      .drain

    o.map(_ => ExitCode.Success)
  }

}

object ReadWriteConcurentStream2 extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = runFrom(None)
}

object ReadWriteConcurentStream3 extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = runFrom(None)
}
