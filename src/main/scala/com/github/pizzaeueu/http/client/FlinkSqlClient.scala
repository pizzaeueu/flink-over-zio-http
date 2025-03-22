package com.github.pizzaeueu.http.client

import com.github.pizzaeueu.codec.FlinkCodec
import com.github.pizzaeueu.config.AppConfig
import com.github.pizzaeueu.domain.{CreateStatement, Session, Statement, StatementStatus}
import zio._
import zio.http._
import zio.json.ast.Json
import zio.schema.codec.JsonCodec._

trait FlinkSqlClient {
  def createSession: Task[Session]
  def createStatement(sessionId: Session): Task[Statement]
  def getStatementStatus(session: Session, statement: Statement): Task[StatementStatus]

  def getQueryResult(session: Session, statement: Statement): Task[Json]
}

case class FlinkSqlClientLive(config: AppConfig, client: Client)
    extends FlinkSqlClient
    with FlinkCodec {

  override def createSession: Task[Session] = {
    for {
      url <- ZIO.fromEither(
        URL.decode(s"${config.flink.host}:${config.flink.port}/v1/sessions")
      )
      res <- client.batched(Request.post(url, Body.empty))
      data <- res.body.to[Session]
      _ <- ZIO.logInfo(s"Session is created - $data")
    } yield data
  }

  override def createStatement(session: Session): Task[Statement] = {
    for {
      url <- ZIO.fromEither(
        URL.decode(
          s"${config.flink.host}:${config.flink.port}/v1/sessions/${session.sessionHandle}/statements"
        )
      )
      createStatement = CreateStatement("SELECT CURRENT_TIMESTAMP;")
      res <- client.batched(Request.post(url, Body.from(createStatement)))
      statement <- res.body.to[Statement]
      _ <- ZIO.logInfo(s"Statement is created - $statement")
    } yield statement
  }

  override def getStatementStatus(session: Session, statement: Statement): Task[StatementStatus] = for {
    url <- ZIO.fromEither(
      URL.decode(
        s"${config.flink.host}:${config.flink.port}/v1/sessions/${session.sessionHandle}/operations/${statement.operationHandle}/status"
      )
    )
    res <- client.batched(Request.get(url))
    data <- res.body.to[StatementStatus]
    _ <- ZIO.logInfo(s"Status of ${statement.operationHandle} statement is ${data.status}")
  } yield data

  override def getQueryResult(session: Session, statement: Statement): Task[Json] = for {
    url <- ZIO.fromEither(
      URL.decode(
        s"${config.flink.host}:${config.flink.port}/v1/sessions/${session.sessionHandle}/operations/${statement.operationHandle}/result/0"
      )
    )
    res <- client.batched(Request.get(url))
    data <- res.body.to[Json]
    _ <- ZIO.logInfo(s"Result of ${statement.operationHandle} statement is ${data.toJsonPretty}")
  } yield data
}

object FlinkSqlClient {
  def live: RLayer[AppConfig with Client, FlinkSqlClientLive] =
    ZLayer.fromFunction(FlinkSqlClientLive.apply _)
}
