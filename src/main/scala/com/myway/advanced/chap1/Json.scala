package com.myway.advanced.chap1
sealed trait Json { self =>
  def asString: String = Json.asString(self)
}
final case class JsObject(get: Map[String, Json]) extends Json
final case class JsString(get: String)            extends Json
final case class JsNumber(get: Double)            extends Json
final case class JsInt(get: Int)                  extends Json

object Json {
  def toJson[A](value: A)(implicit w: JsonWriter[A]): Json =
    w.write(value)
  def asString(json: Json): String = json match {
    case JsString(s) => s"\"$s\""
    case JsNumber(d) => s"\"$d\""
    case JsInt(d)    => s"\"$d\""
    case JsObject(map) =>
      val s =
        map.toList.map { case (k, v) => s"${asString(JsString(k))}:${asString(v)}" }.mkString(",")
      s"{$s}"
  }
}

trait JsonWriter[A] {
  def write(value: A): Json
}
object JsonWriterInstances {

  implicit val stringJsonWriter = new JsonWriter[String] {
    def write(value: String): Json = JsString(value)
  }

  implicit val doubleJsonWriter = new JsonWriter[Double] {
    def write(value: Double): Json = JsNumber(value)
  }
  implicit val intJsonWriter = new JsonWriter[Int] {
    def write(value: Int): Json = JsNumber(value)
  }

}
