package com.github.pizzaeueu.http.client

import com.github.pizzaeueu.codec.FlinkCodec
import com.github.pizzaeueu.config.AppConfig
import com.github.pizzaeueu.domain.{
  CreateStatement,
  Session,
  Statement,
  StatementStatus
}
import zio._
import zio.http._
import zio.json._
import zio.json.ast.Json.Arr
import zio.json.ast.{Json, JsonCursor}
import zio.schema.codec.JsonCodec._

import scala.language.postfixOps

trait FlinkSqlClient {
  def createSession: Task[Session]
  def createStatement(sessionId: Session, sql: String): Task[Statement]
  def getStatementStatus(
      session: Session,
      statement: Statement
  ): Task[StatementStatus]

  def getJobResult(session: Session, statement: Statement): Task[Json]
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

  override def createStatement(
      session: Session,
      sql: String
  ): Task[Statement] = {
    for {
      url <- ZIO.fromEither(
        URL.decode(
          s"${config.flink.host}:${config.flink.port}/v1/sessions/${session.sessionHandle}/statements"
        )
      )
      createStatement = CreateStatement(sql)
      res <- client.batched(Request.post(url, Body.from(createStatement)))
      statement <- res.body.to[Statement]
      _ <- ZIO.logInfo(s"Statement is created - $statement")
    } yield statement
  }

  override def getStatementStatus(
      session: Session,
      statement: Statement
  ): Task[StatementStatus] = for {
    url <- ZIO.fromEither(
      URL.decode(
        s"${config.flink.host}:${config.flink.port}/v1/sessions/${session.sessionHandle}/operations/${statement.operationHandle}/status"
      )
    )
    res <- client.batched(Request.get(url))
    data <- res.body.to[StatementStatus]
    _ <- ZIO.logInfo(
      s"Status of ${statement.operationHandle} statement is ${data.status}"
    )
  } yield data

  override def getJobResult(
      session: Session,
      statement: Statement
  ): Task[Json] = {

    // TODO: Rewrite to stack safe ( tail ) recursive call
    def loop(
        partition: Int,
        jsonAcc: Arr
    ): Task[Json] = {
      for {
        json <- loadPartition(session, statement, partition)
        resultArray = Arr(jsonAcc.elements :+ json)
        nextUri = json.get(JsonCursor.field("nextResultUri").isString)
        res <- nextUri match {
          case Left(_) => ZIO.succeed(resultArray)
          case Right(_) =>
            ZIO.sleep(50 milliseconds) *> loop(
              partition + 1,
              resultArray
            )
        }
      } yield res
    }
    for {
      jsonResult <- loop(0, Json.Arr.empty)
      _ <- ZIO.logInfo(
        s"Result of ${statement.operationHandle} statement is ${jsonResult.toJsonPretty}"
      )
    } yield jsonResult
  }

  private def loadPartition(
      session: Session,
      statement: Statement,
      partition: Int
  ): Task[Json] = {
    for {
      url <- ZIO.fromEither(
        URL.decode(
          s"${config.flink.host}:${config.flink.port}/v1/sessions/${session.sessionHandle}/operations/${statement.operationHandle}/result/$partition"
        )
      )
      res <- client.batched(Request.get(url))
      json <- res.body.to[Json]
    } yield json
  }
}

object FlinkSqlClient {
  def live: RLayer[AppConfig with Client, FlinkSqlClientLive] =
    ZLayer.fromFunction(FlinkSqlClientLive.apply _)
}
