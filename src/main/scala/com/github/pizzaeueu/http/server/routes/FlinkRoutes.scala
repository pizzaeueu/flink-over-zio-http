package com.github.pizzaeueu.http.server.routes

import com.github.pizzaeueu.codec.FlinkCodec
import com.github.pizzaeueu.domain.SqlRequest
import com.github.pizzaeueu.service.FlinkSqlService
import zio._
import zio.http._
import zio.json._

trait FlinkRoutes {
  def build(): Routes[Any, Nothing]
}

final case class FlinkRoutesLive(service: FlinkSqlService)
    extends FlinkRoutes
    with FlinkCodec {

  private implicit val sqlRequestDecoder: JsonDecoder[SqlRequest] =
    DeriveJsonDecoder.gen[SqlRequest]

  override def build(): Routes[Any, Nothing] =
    Routes(
      Method.POST / Root / "api" / "run-sql" -> handler { (request: Request) =>
        for {
          bodyStr <- request.body.asString
          sqlRequest <- ZIO.fromEither(
            bodyStr
              .fromJson[SqlRequest]
              .left
              .map(err => new RuntimeException(err))
          )
          response <- service.runSql(sqlRequest.query)
        } yield Response.text(response.toJsonPretty)
      }
    ).handleError(err => Response.internalServerError(err.getMessage))

}

object FlinkRoutesLive {
  val live: RLayer[FlinkSqlService, FlinkRoutesLive] =
    ZLayer.fromFunction(FlinkRoutesLive.apply _)
}
