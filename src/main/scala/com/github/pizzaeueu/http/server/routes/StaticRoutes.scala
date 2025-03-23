package com.github.pizzaeueu.http.server.routes

import zio.{ULayer, ZLayer}
import zio.http.{Middleware, Path, Routes}

trait StaticRoutes {
  def build(): Routes[Any, Nothing]
}

final case class StaticRoutesLive() extends StaticRoutes {
  override def build(): Routes[Any, Nothing] = Routes.empty @@ Middleware.serveResources(Path.empty / "app", "static")
}

object StaticRoutes {
  def live: ULayer[StaticRoutesLive] = ZLayer.succeed(StaticRoutesLive())
}
