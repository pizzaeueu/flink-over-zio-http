package com.github.pizzaeueu

import com.github.pizzaeueu.config.Configuration
import com.github.pizzaeueu.domain.FlinkQueryResult
import com.github.pizzaeueu.http.client.FlinkSqlClient
import com.github.pizzaeueu.http.server.routes.{FlinkRoutesLive, StaticRoutes}
import com.github.pizzaeueu.http.server.{HttpServer, HttpServerLive}
import com.github.pizzaeueu.repository.FlinkQueryRepositoryInMemory
import com.github.pizzaeueu.service.FlinkSqlService
import zio.http.{Client, Server}
import zio.{Ref, ZIO, ZIOAppDefault, ZLayer}

object Main extends ZIOAppDefault {
  override def run =
    ZIO
      .suspend(Configuration.provider)
      .flatMap { config =>
        Ref.make[List[FlinkQueryResult]](List.empty).map(ref => (ref, config))
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
            FlinkQueryRepositoryInMemory.live,
            ZLayer.succeed(ref)
          )
      }
}
