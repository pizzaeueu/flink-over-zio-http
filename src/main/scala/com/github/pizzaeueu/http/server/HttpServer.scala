package com.github.pizzaeueu.http.server

import com.github.pizzaeueu.http.server.routes.{FlinkRoutes, StaticRoutes}
import com.github.pizzaeueu.service.FlinkSqlService
import zio.http.{Root, Server}
import zio.{URIO, ZLayer}

trait HttpServer {
  def start: URIO[Server, Nothing]
}

case class HttpServerLive(
    flinkRoutes: FlinkRoutes,
    staticRoutes: StaticRoutes,
    service: FlinkSqlService
) extends HttpServer {

  override def start: URIO[Server, Nothing] =
    Server.serve(flinkRoutes.build(service) ++ staticRoutes.build())
}

object HttpServerLive {
  private[server] val ApiPath = Root / "api"
  def live: ZLayer[
    FlinkRoutes with StaticRoutes with FlinkSqlService,
    Nothing,
    HttpServerLive
  ] =
    ZLayer.fromFunction(HttpServerLive.apply _)
}
