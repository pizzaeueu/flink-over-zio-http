package com.github.pizzaeueu

import com.github.pizzaeueu.config.Configuration
import com.github.pizzaeueu.domain.FlinkJobResult
import com.github.pizzaeueu.http.client.FlinkSqlClient
import com.github.pizzaeueu.http.server.routes.{FlinkRoutesLive, StaticRoutes}
import com.github.pizzaeueu.http.server.{HttpServer, HttpServerLive}
import com.github.pizzaeueu.repository.FlinkJobRepositoryInMemory
import com.github.pizzaeueu.service.FlinkSqlService
import zio.http.{Client, Server}
import zio.{Ref, ZIO, ZIOAppDefault, ZLayer}

object Main extends ZIOAppDefault {
  override def run =
    ZIO
      .suspend(Configuration.provider)
      .flatMap { config =>
        Ref.make[List[FlinkJobResult]](List.empty).map(ref => (ref, config))
      }
      .flatMap { case (ref, config) =>
        ZIO
          .serviceWithZIO[HttpServer](_.start)
          .provide(
            HttpServerLive.live,
            FlinkRoutesLive.live,
            StaticRoutes.live,
            Server.defaultWithPort(config.server.port),
            FlinkSqlClient.live,
            FlinkSqlService.live,
            ZLayer.succeed(config),
            Client.default,
            FlinkJobRepositoryInMemory.live,
            ZLayer.succeed(ref)
          )
      }
}
