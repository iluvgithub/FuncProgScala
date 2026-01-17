package com.myway.pfpvolpeg.shopping.serialization

import com.myway.pfpvolpeg.shopping.domain.{Okay, Status, Unreachable}
import io.circe.Encoder
import monocle.Iso

object StatusReadWrite {

  val _Bool: Iso[Status, Boolean] =
    Iso[Status, Boolean] {
      case Okay=> true
      case Unreachable=> false
    }(if (_) Okay else Unreachable)
  implicit val jsonEncoder: Encoder[Status] =
    Encoder.forProduct1("status")(_.toString)
}