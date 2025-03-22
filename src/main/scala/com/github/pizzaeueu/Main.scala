package com.github.pizzaeueu

import com.github.pizzaeueu.config.Configuration
import com.github.pizzaeueu.http.{FlinkRoutesLive, HttpServer, HttpServerLive}
import zio.http.Server
import zio.{ZIO, ZIOAppDefault}

object Main extends ZIOAppDefault {
  override def run =
    ZIO.suspend(Configuration.provider).flatMap { _ =>
      ZIO
        .serviceWithZIO[HttpServer](_.start)
        .provide(
          HttpServerLive.live,
          FlinkRoutesLive.live,
          Server.default
        )
    }
}
