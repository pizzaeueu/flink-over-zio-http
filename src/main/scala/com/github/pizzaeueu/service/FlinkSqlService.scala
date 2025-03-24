package com.github.pizzaeueu.service

import com.github.pizzaeueu.domain.{FlinkQueryResult, Session, Statement, StatementStatus}
import com.github.pizzaeueu.http.client.FlinkSqlClient
import com.github.pizzaeueu.repository.FlinkQueryRepository
import zio.json.ast.Json
import zio.{RLayer, Task, ZIO, ZLayer}

trait FlinkSqlService {
  def runSql(sql: String): Task[Json]
  def loadQueries(): Task[List[FlinkQueryResult]]
  def loadQueryById(queryId: String): Task[Option[FlinkQueryResult]]
}

case class FlinkSqlServiceLive(
    flinkClient: FlinkSqlClient,
    flinkQueryRepository: FlinkQueryRepository
) extends FlinkSqlService {

  override def runSql(sql: String): Task[Json] = for {
    session <- flinkClient.createSession
    statement <- flinkClient.createStatement(session, sql)
    statementStatus <- pingUntilFlinkQuerySucceed(session, statement)
    _ <- failIfFlinkQueryNotSucceed(statementStatus, session, statement)
    queryResult <- flinkClient.getQueryResult(session, statement)
    queryId = s"${session.sessionHandle}->${statement.operationHandle}"
    flinkQueryResult = FlinkQueryResult(queryId, queryResult)
    _ <- flinkQueryRepository.saveJob(flinkQueryResult)
  } yield queryResult

  private def pingUntilFlinkQuerySucceed(
      session: Session,
      statement: Statement
  ) =
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

  override def loadQueries(): Task[List[FlinkQueryResult]] =
    flinkQueryRepository.loadJobs

  override def loadQueryById(queryId: String): Task[Option[FlinkQueryResult]] = ???
}

object FlinkSqlService {
  def live
      : RLayer[FlinkSqlClient with FlinkQueryRepository, FlinkSqlServiceLive] =
    ZLayer.fromFunction(FlinkSqlServiceLive.apply _)
}
