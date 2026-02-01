package com.myway.sampleapp.redis
import cats.effect.{IO, Resource}
import dev.profunktor.redis4cats.RedisCommands
import munit.CatsEffectSuite
import org.mockito.IdiomaticMockito
import org.mockito.cats.IdiomaticMockitoCats
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
class RedisReadWriteSuite
    extends CatsEffectSuite
    with Matchers
    with IdiomaticMockito
    with IdiomaticMockitoCats {
  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  val mockCmd: RedisCommands[IO, String, String] = mock[RedisCommands[IO, String, String]]
  val mockResource: Resource[IO, RedisCommands[IO, String, String]] =
    Resource.pure[IO, RedisCommands[IO, String, String]](mockCmd)

  val mockClient: RedisClient[IO] = mock[RedisClient[IO]]
  mockClient.commands returns mockResource // no F needed here

  test("read ") {
    // Arrange
    mockCmd.get("user:123") returnsF Some("token-xyz") // ← returnsF syntax
    // or
    // mockCmd.get("user:123") returnsF IO.pure(Some("token-xyz"))
    // mockCmd.get("user:123") raisesF new RuntimeException("boom")

    val reader = new RedisReader[IO](mockClient)

    // Act
    val out: IO[Option[String]] = reader.read("user:123")
    // Assert
    out.map { result =>
      result shouldBe Some("token-xyz")
      mockCmd.get("user:123") wasCalled once

      mockCmd.get("user:124") wasNever called
    }
  }

  test("write ") {
    // Arrange
    mockCmd.get("user:123") returnsF Some("token-xyz")

    val writer = new RedisWriter[IO](mockClient)

    // Act
    val out: IO[Unit] = writer.write("user:123", "value.6789")
    // Assert
    out.map { result =>
      // Verification is also syntax sugar
      mockCmd.set("user:123", "value.6789") wasCalled once
      mockCmd.set("user:124", "value.6789") wasNever called
    }
  }
}
