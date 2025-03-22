package com.github.pizzaeueu.service

import com.github.pizzaeueu.http.client.FlinkSqlClient
import zio.{RLayer, Task, ZLayer}

trait FlinkSqlService {
  def runSql(sql: String): Task[String]
}

case class FlinkSqlServiceLive(clinkClient: FlinkSqlClient)
    extends FlinkSqlService {

  override def runSql(sql: String): Task[String] = clinkClient.createSession
}

object FlinkSqlService {
  def live: RLayer[FlinkSqlClient, FlinkSqlServiceLive] =
    ZLayer.fromFunction(FlinkSqlServiceLive.apply _)
}
