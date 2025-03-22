package com.github.pizzaeueu.http.client

import com.github.pizzaeueu.config.AppConfig
import zio._
import zio.http._

trait FlinkSqlClient {
  def createSession: Task[String]
}

case class FlinkSqlClientLive(config: AppConfig, client: Client)
    extends FlinkSqlClient {

  override def createSession: Task[String] = {
    for {
      url <- ZIO.fromEither(
        URL.decode(s"${config.flink.host}:${config.flink.port}/v1/sessions")
      )
      res <- client.batched(Request.post(url, Body.empty))
      data <- res.body.asString
      _ <- ZIO.logInfo(s"Session is created - $data")
    } yield data
  }
}

object FlinkSqlClient {
  def live: RLayer[AppConfig with Client, FlinkSqlClientLive] =
    ZLayer.fromFunction(FlinkSqlClientLive.apply _)
}
