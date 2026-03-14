package com.myway.advanced.chap1

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class JsonTest extends AnyFunSuite {

  test("simple serialization into String ") {
    // arrange
    final case class Person(name: String, email: String)
    implicit val personJsonWriter: JsonWriter[Person] = new JsonWriter[Person] {
      def write(value: Person): Json = JsObject(
        Map(
          "name"  -> JsString(value.name),
          "email" -> JsString(value.email)
        )
      )
    }
    implicit class JsonWriterOps[A](value: A) {
      def toJson(implicit w: JsonWriter[A]): Json =
        w.write(value)
    }
    val p = Person("Toto", "toto@gmail.com")
    // act
    val json: Json = p.toJson
    // assert
    json.asString shouldBe "{\"name\":\"Toto\",\"email\":\"toto@gmail.com\"}"
  }

}
