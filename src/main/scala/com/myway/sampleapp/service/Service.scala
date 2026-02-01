package com.myway.sampleapp.service

import cats.effect.Async
import org.typelevel.log4cats.Logger

trait Service [F[_] ]{

}

case class SampleService[F[_]: Async: Logger]() extends Service [F]{

}