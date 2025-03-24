package com.github.pizzaeueu.service.mock

import com.github.pizzaeueu.domain.FlinkJobResult
import com.github.pizzaeueu.repository.FlinkJobRepository
import zio.json.ast.Json
import zio.{Task, ULayer, ZIO, ZLayer}

object FlinkJobRepositoryMock {
  val mock: ULayer[FlinkJobRepository] = ZLayer.succeed {
    new FlinkJobRepository {
      override def loadJobs: Task[List[FlinkJobResult]] =
        ZIO.succeed(
          List(
            FlinkJobResult("1", Json.Null),
            FlinkJobResult("2", Json.Null)
          )
        )

      override def saveJob(job: FlinkJobResult): Task[Unit] = ZIO.unit
    }
  }

}
