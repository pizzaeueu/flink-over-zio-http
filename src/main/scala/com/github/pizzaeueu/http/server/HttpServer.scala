package com.github.pizzaeueu.http.server

import com.github.pizzaeueu.http.server.routes.FlinkRoutes
import zio.http.Server
import zio.{URIO, ZLayer}

trait HttpServer {
  def start: URIO[Server, Nothing]
}

case class HttpServerLive(routes: FlinkRoutes) extends HttpServer {

  override def start: URIO[Server, Nothing] = Server.serve(routes.build())
}

object HttpServerLive {
  def live: ZLayer[FlinkRoutes, Nothing, HttpServerLive] =
    ZLayer.fromFunction(HttpServerLive.apply _)
}
