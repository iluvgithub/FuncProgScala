package com.myway.sampleapp.routes

import cats.data.Kleisli
import cats.effect.IO
import com.myway.sampleapp.routes.HttpRoute._
import com.myway.sampleapp.service.Service
import munit.CatsEffectSuite
import org.http4s.Method._
import org.http4s._
import org.http4s.implicits._
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.{verify, when}
import org.mockito.MockitoSugar.mock

class HttpRouteSuite extends CatsEffectSuite {

  val mockService: Service[IO] = mock[Service[IO]]

  def routeToTest: Kleisli[IO, Request[IO], Response[IO]] = routes[IO](mockService).orNotFound

  test("GET /health returns 200 OK") {
    val req = Request[IO](GET, uri"/health")
    routeToTest.run(req).flatMap { resp =>
      resp.as[String].map { body =>
        assertEquals(resp.status, Status.Ok)
        assertEquals(body, "OK")
      }
    }
  }

  test("POST /echo returns body") {
    val req = Request[IO](POST, uri"/echo").withEntity("hello server")
    routeToTest.run(req).flatMap { resp =>
      resp.as[String].map { body =>
        assertEquals(resp.status, Status.Ok)
        assertEquals(body, "Received: HELLO SERVER")
      }
    }
  }

  test("GET /echo?arg=toto returns TOTO") {
    val req = Request[IO](GET, uri"/service?arg=toto")
    when(mockService.sampleService(anyString())).thenReturn(IO("TOTO"))
    routeToTest.run(req).flatMap { resp =>
      resp.as[String].map { body =>
        assertEquals(resp.status, Status.Ok)
        assert(body.contains("TOTO"))
        verify(mockService).sampleService("toto")
      }
    }
  }

  test("GET /echo without arg1 returns 400 BadRequest") {
    val req = Request[IO](GET, uri"/service")
    routeToTest.run(req).map { resp =>
      assertEquals(resp.status, Status.BadRequest)
    }
  }

  test("GET /no such route returns 400 BadRequest") {
    val req = Request[IO](GET, uri"/noSuchRoute")
    routeToTest.run(req).map { resp =>
      assertEquals(resp.status, Status.NotFound)
    }
  }

  test("GET /redis read") {
    val req = Request[IO](GET, uri"/redis/read?key=abc")
    when(mockService.redisRead(anyString())).thenReturn(IO("out"))
    routeToTest.run(req).flatMap { resp =>
      assertEquals(resp.status, Status.Ok)
      verify(mockService).redisRead("abc")
      resp.as[String].map { body =>
        assertEquals(body, "redis.read:out")
      }
    }
  }

  test("GET /redis write") {
    val req = Request[IO](GET, uri"/redis/write?key=abc&value=Abcd")
    when(mockService.redisWrite("abc", "Abcd")).thenReturn(IO(()))
    routeToTest.run(req).map { resp =>
      assertEquals(resp.status, Status.Ok)
      verify(mockService).redisWrite("abc", "Abcd")
    }
  }
}
