package com.myway.sampleapp.routes

import cats.data.Kleisli
import cats.effect.IO
import munit.CatsEffectSuite
import org.http4s.Method._
import org.http4s._
import org.http4s.implicits._
import com.myway.sampleapp.routes.HttpRoute._
import com.myway.sampleapp.service.Service
import org.mockito.MockitoSugar.mock

class HttpRouteSuite extends CatsEffectSuite {

  val mockService: Service[IO] = mock[Service[IO]]

  val routeToTest: Kleisli[IO, Request[IO], Response[IO]] =  routes[IO](mockService).orNotFound
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

  test("GET /echo?arg1=toto returns arg1") {
    val req = Request[IO](GET, uri"/service?arg=toto")
    routeToTest.run(req).flatMap { resp =>
      resp.as[String].map { body =>
        assertEquals(resp.status, Status.Ok)
        assert(body.contains("toto"))
      }
    }
  }

  test("GET /echo without arg1 returns 400 BadRequest") {
    val req = Request[IO](GET, uri"/service")
    routeToTest.run(req).map { resp =>
      assertEquals(resp.status, Status.BadRequest)
    }
  }


  test("GET /redis read") {
    val req = Request[IO](GET, uri"/redis/read")
    routeToTest.run(req).map { resp =>
      assertEquals(resp.status, Status.Ok)
    }
  }


  test("GET /redis write") {
    val req = Request[IO](GET, uri"/redis/write")
    routeToTest.run(req).map { resp =>
      assertEquals(resp.status, Status.Ok)
    }
  }
}
