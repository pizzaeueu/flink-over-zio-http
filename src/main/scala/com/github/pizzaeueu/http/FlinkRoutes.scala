package com.github.pizzaeueu.http

import zio._
import zio.http._

trait FlinkRoutes {
  def build(): Routes[Any, Nothing]
}

final case class FlinkRoutesLive() extends FlinkRoutes {

  override def build(): Routes[Any, Nothing] =
    Routes(
      Method.GET / Root -> handler(Response.text("Hello World"))
    )
}

object FlinkRoutesLive {
  val live: ULayer[FlinkRoutes] = ZLayer.succeed(FlinkRoutesLive.apply())
}
