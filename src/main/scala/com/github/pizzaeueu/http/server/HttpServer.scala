package com.github.pizzaeueu.http.server

import com.github.pizzaeueu.http.server.routes.{FlinkRoutes, StaticRoutes}
import zio.http.Server
import zio.{URIO, ZLayer}

trait HttpServer {
  def start: URIO[Server, Nothing]
}

case class HttpServerLive(flinkRoutes: FlinkRoutes, staticRoutes: StaticRoutes) extends HttpServer {

  override def start: URIO[Server, Nothing] = Server.serve(flinkRoutes.build() ++ staticRoutes.build())
}

object HttpServerLive {
  def live: ZLayer[FlinkRoutes with StaticRoutes, Nothing, HttpServerLive] =
    ZLayer.fromFunction(HttpServerLive.apply _)
}
