package com.github.pizzaeueu.http.server.routes

import com.github.pizzaeueu.service.FlinkSqlService
import zio._
import zio.http._

trait FlinkRoutes {
  def build(): Routes[Any, Nothing]
}

final case class FlinkRoutesLive(service: FlinkSqlService) extends FlinkRoutes {

  override def build(): Routes[Any, Nothing] =
    Routes(
      Method.GET / Root -> handler(
        service
          .runSql("")
          .map(sessionId => Response.text(sessionId))
          .onError(err => ZIO.logError(err.prettyPrint))
          .orDie
      )
    )
}

object FlinkRoutesLive {
  val live: RLayer[FlinkSqlService, FlinkRoutesLive] =
    ZLayer.fromFunction(FlinkRoutesLive.apply _)
}
