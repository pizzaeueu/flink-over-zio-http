package com.github.pizzaeueu.http.server.routes

import com.github.pizzaeueu.codec.FlinkCodec
import com.github.pizzaeueu.domain.{FlinkJobResult, SqlRequest}
import com.github.pizzaeueu.http.server.HttpServerLive.ApiPath
import com.github.pizzaeueu.service.FlinkSqlService
import zio._
import zio.http._
import zio.json._
import zio.http.codec.PathCodec.string

trait FlinkRoutes {
  def build(service: FlinkSqlService): Routes[Any, Nothing]
}

final case class FlinkRoutesLive(service: FlinkSqlService)
    extends FlinkRoutes
    with FlinkCodec {

  private implicit val sqlRequestDecoder: JsonDecoder[SqlRequest] =
    DeriveJsonDecoder.gen[SqlRequest]
  private implicit val flinkJobResultEncoder: JsonEncoder[FlinkJobResult] =
    DeriveJsonEncoder.gen[FlinkJobResult]

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
          response <- service.runSql(sqlRequest.sql)
        } yield Response.text(response.toJsonPretty)
      },
      Method.GET / ApiPath / "jobs" -> handler {
        for {
          queries <- service.loadJobs()
          res = queries.toJsonPretty
        } yield Response.json(res)
      },
      Method.GET / ApiPath / "jobs" / string("jobId") -> handler {
        (jobId: String, _: Request) =>
          for {
            jobResult <- service.loadJobsById(jobId)
            response = jobResult match {
              case Some(result) => Response.json(result.toJsonPretty)
              case None         => Response.notFound(s"Job $jobId not found")
            }
          } yield response
      }
    ).handleError(err => Response.internalServerError(err.getMessage))
}

object FlinkRoutesLive {
  val live: RLayer[FlinkSqlService, FlinkRoutesLive] =
    ZLayer.fromFunction(FlinkRoutesLive.apply _)
}
