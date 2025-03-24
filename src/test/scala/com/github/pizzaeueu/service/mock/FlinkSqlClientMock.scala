package com.github.pizzaeueu.service.mock

import com.github.pizzaeueu.domain.{Session, Statement, StatementStatus}
import com.github.pizzaeueu.http.client.FlinkSqlClient
import zio.{Task, ULayer, ZIO, ZLayer}
import zio.json.ast.Json

object FlinkSqlClientMock {
  val mock: ULayer[FlinkSqlClient] = ZLayer.succeed(new FlinkSqlClient {
    override def createSession: Task[Session] = ZIO.succeed(Session("session"))

    override def createStatement(
        sessionId: Session,
        sql: String
    ): Task[Statement] = ZIO.succeed(Statement("statement"))

    override def getStatementStatus(
        session: Session,
        statement: Statement
    ): Task[StatementStatus] = ZIO.succeed(StatementStatus.Finished)

    override def getJobResult(
        session: Session,
        statement: Statement
    ): Task[Json] = ZIO.succeed(Json.Arr.empty)
  })
}
