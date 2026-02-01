package com.myway.sampleapp.service

import cats.effect.Async
import org.typelevel.log4cats.Logger

trait Service [F[_] ] {

  def sampleService(arg:String):F[String]
}

case class SampleService[F[_]: Async: Logger]() extends Service [F]{

  override def sampleService(arg: String): F[String] =
    Async[F].pure(arg.toUpperCase)

}