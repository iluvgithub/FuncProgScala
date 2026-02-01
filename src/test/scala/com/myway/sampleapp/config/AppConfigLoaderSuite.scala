package com.myway.sampleapp.config
import cats.effect.{IO, Resource}
import com.comcast.ip4s.{Host, Port}
import munit.CatsEffectSuite
import pureconfig.ConfigSource

import java.io.{File, PrintWriter}
class AppConfigLoaderSuite extends CatsEffectSuite {
  private def withTempConfig(content: String)(writer: ConfigSource => IO[Unit]): IO[Unit] = {
    val tempFile = File.createTempFile("app-config-test", ".conf")
    tempFile.deleteOnExit()

    Resource
      .fromAutoCloseable(IO(new PrintWriter(tempFile)))
      .use(writer => IO(writer.write(content)) *> IO(writer.flush()))
      .flatMap(_ => writer(ConfigSource.file(tempFile)))
  }
  test("AppConfigLoader should correctly load valid minimal configuration") {
    val configContent =
      """
        |
        |http-config {
        |  host = "127.0.0.1"
        |  port = 8080
        |}
        |
        |redis-config {
        |  host = "localhost"
        |  port = 6379
        |}
        |""".stripMargin

    withTempConfig(configContent) { source =>
      for {
        appConfig <- AppConfigLoader.loadConfig[IO](source)
        http      <- appConfig.getHttpServerConfig[IO]
        redis = appConfig.redisConfig
      } yield {
        assertEquals(http.host, Host.fromString("127.0.0.1").get)
        assertEquals(http.port, Port.fromInt(8080).get)
        assertEquals(redis.host, "localhost")
        assertEquals(redis.port, 6379)
      }
    }
  }
}
