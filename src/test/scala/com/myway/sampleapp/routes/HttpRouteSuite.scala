package com.myway.sampleapp.routes

import cats.effect.IO
import munit.CatsEffectSuite
import org.http4s.Method._
import org.http4s._
import org.http4s.implicits._
import com.myway.sampleapp.routes.HttpRoute._

class HttpRouteSuite extends CatsEffectSuite {

  test("GET /health returns 200 OK") {
    val req = Request[IO](GET, uri"/health")
    routes[IO].orNotFound.run(req).flatMap { resp =>
      resp.as[String].map { body =>
        assertEquals(resp.status, Status.Ok)
        assertEquals(body, "OK")
      }
    }
  }

  test("POST /echo returns body") {
    val req = Request[IO](POST, uri"/echo").withEntity("hello server")
    routes[IO].orNotFound.run(req).flatMap { resp =>
      resp.as[String].map { body =>
        assertEquals(resp.status, Status.Ok)
        assertEquals(body, "Received: HELLO SERVER")
      }
    }
  }

  test("GET /echo?arg1=toto returns arg1") {
    val req = Request[IO](GET, uri"/service?arg=toto")
    routes[IO].orNotFound.run(req).flatMap { resp =>
      resp.as[String].map { body =>
        assertEquals(resp.status, Status.Ok)
        assert(body.contains("toto"))
      }
    }
  }

  test("GET /echo without arg1 returns 400 BadRequest") {
    val req = Request[IO](GET, uri"/service")
    routes[IO].orNotFound.run(req).map { resp =>
      assertEquals(resp.status, Status.BadRequest)
    }
  }
}
