package com.github.pizzaeueu.service

import com.github.pizzaeueu.domain.{Session, Statement, StatementStatus}
import com.github.pizzaeueu.http.client.FlinkSqlClient
import zio.json.ast.Json
import zio.{RLayer, Task, ZIO, ZLayer}

trait FlinkSqlService {
  def runSql(sql: String): Task[Json]
}

case class FlinkSqlServiceLive(flinkClient: FlinkSqlClient)
    extends FlinkSqlService {

  override def runSql(sql: String): Task[Json] = for {
    session <- flinkClient.createSession
    statement <- flinkClient.createStatement(session, sql)
    statementStatus <- pingUntilFlinkQuerySucceed(session, statement)
    _ <- failIfFlinkQueryNotSucceed(statementStatus, session, statement)
    queryResult <- flinkClient.getQueryResult(session, statement)
  } yield queryResult

  private def pingUntilFlinkQuerySucceed(session: Session, statement: Statement) =
    flinkClient
      .getStatementStatus(session, statement)
      .repeatWhile(
        Set(StatementStatus.Running, StatementStatus.Pending).contains
      )

  private def failIfFlinkQueryNotSucceed(
      statementStatus: StatementStatus,
      session: Session,
      statement: Statement
  ): Task[Unit] =
    ZIO.ifZIO(ZIO.succeed(statementStatus == StatementStatus.Finished))(
      ZIO.unit,
      ZIO.logError(
        s"Error during operation execution: session: ${session.sessionHandle}, operation: ${statement.operationHandle}, status: ${statementStatus.status}"
      ) *> ZIO.fail(
        new RuntimeException(
          s"Operation failed - ${statementStatus.status}, operation id - ${statement.operationHandle}"
        )
      )
    )
}

object FlinkSqlService {
  def live: RLayer[FlinkSqlClient, FlinkSqlServiceLive] =
    ZLayer.fromFunction(FlinkSqlServiceLive.apply _)
}
