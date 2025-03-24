package com.github.pizzaeueu.service

import com.github.pizzaeueu.domain.{
  FlinkJobResult,
  Session,
  Statement,
  StatementStatus
}
import com.github.pizzaeueu.http.client.FlinkSqlClient
import com.github.pizzaeueu.repository.FlinkJobRepository
import zio.json.ast.{Json, JsonCursor}
import zio.{RLayer, Task, ZIO, ZLayer}

import java.util.UUID

trait FlinkSqlService {
  def runSql(sql: String): Task[Json]
  def loadJobs(): Task[List[FlinkJobResult]]
  def loadJobsById(jobId: String): Task[Option[FlinkJobResult]]
}

case class FlinkSqlServiceLive(
    flinkClient: FlinkSqlClient,
    flinkJobRepository: FlinkJobRepository
) extends FlinkSqlService {

  override def runSql(sql: String): Task[Json] = for {
    session <- flinkClient.createSession
    statement <- flinkClient.createStatement(session, sql)
    statementStatus <- pingUntilFlinkJobSucceed(session, statement)
    _ <- failIfFlinkJobNotSucceed(statementStatus, session, statement)
    jobResult <- flinkClient.getJobResult(session, statement)
    jobId = generateJobId(jobResult)
    flinkJobResult = FlinkJobResult(jobId, jobResult)
    _ <- flinkJobRepository.saveJob(flinkJobResult)
  } yield jobResult

  override def loadJobs(): Task[List[FlinkJobResult]] =
    flinkJobRepository.loadJobs

  override def loadJobsById(jobId: String): Task[Option[FlinkJobResult]] =
    loadJobs().map(_.find(_.jobId == jobId))

  private def pingUntilFlinkJobSucceed(
      session: Session,
      statement: Statement
  ) =
    flinkClient
      .getStatementStatus(session, statement)
      .repeatWhile(
        Set(StatementStatus.Running, StatementStatus.Pending).contains
      )

  private def failIfFlinkJobNotSucceed(
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

  private def generateJobId(jobResult: Json): String = jobResult
    .get(JsonCursor.field("jobId").isString)
    .toOption
    .map(_.value)
    .getOrElse(UUID.randomUUID().toString)
}

object FlinkSqlService {
  def live
      : RLayer[FlinkSqlClient with FlinkJobRepository, FlinkSqlServiceLive] =
    ZLayer.fromFunction(FlinkSqlServiceLive.apply _)
}
