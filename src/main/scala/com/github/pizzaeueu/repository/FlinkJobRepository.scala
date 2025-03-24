package com.github.pizzaeueu.repository

import com.github.pizzaeueu.domain.FlinkJobResult
import zio.{Ref, Task, URLayer, ZLayer}

trait FlinkJobRepository {
  def loadJobs: Task[List[FlinkJobResult]]

  def saveJob(job: FlinkJobResult): Task[Unit]
}

case class FlinkJobRepositoryInMemory(ref: Ref[List[FlinkJobResult]])
    extends FlinkJobRepository {
  override def loadJobs: Task[List[FlinkJobResult]] = ref.get

  override def saveJob(job: FlinkJobResult): Task[Unit] =
    ref.update { allJobs =>
      allJobs :+ job
    }
}

object FlinkJobRepositoryInMemory {
  def live: URLayer[Ref[List[FlinkJobResult]], FlinkJobRepositoryInMemory] =
    ZLayer.fromFunction(FlinkJobRepositoryInMemory.apply _)
}
