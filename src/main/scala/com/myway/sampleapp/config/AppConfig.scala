package com.myway.sampleapp.config

case class AppConfig(httpServerConfig: HttpServerConfig)

import com.comcast.ip4s._

case class HttpServerConfig(
  host: Host,
  port: Port
)
