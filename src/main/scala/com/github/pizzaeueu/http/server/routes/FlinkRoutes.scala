package com.github.pizzaeueu.http.server.routes

import com.github.pizzaeueu.codec.FlinkCodec
import com.github.pizzaeueu.domain.{FlinkQueryResult, SqlRequest}
import com.github.pizzaeueu.http.server.HttpServerLive.ApiPath
import com.github.pizzaeueu.service.FlinkSqlService
import zio._
import zio.http.{Method, _}
import zio.json._

trait FlinkRoutes {
  def build(service: FlinkSqlService): Routes[Any, Nothing]
}

final case class FlinkRoutesLive(service: FlinkSqlService)
    extends FlinkRoutes
    with FlinkCodec {

  private implicit val sqlRequestDecoder: JsonDecoder[SqlRequest] =
    DeriveJsonDecoder.gen[SqlRequest]
  private implicit val flinkQueryResultEncoder: JsonEncoder[FlinkQueryResult] =
    DeriveJsonEncoder.gen[FlinkQueryResult]

  override def build(service: FlinkSqlService): Routes[Any, Nothing] =
    Routes(
      Method.POST / ApiPath / "run-sql" -> handler { (request: Request) =>
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
      },
      Method.GET / ApiPath / "queries" -> handler {
        for {
          queries <- service.loadQueries()
          res = queries.toJsonPretty
        } yield Response.json(res)
      }
    ).handleError(err => Response.internalServerError(err.getMessage))

}

object FlinkRoutesLive {
  val live: RLayer[FlinkSqlService, FlinkRoutesLive] =
    ZLayer.fromFunction(FlinkRoutesLive.apply _)
}
