package com.github.pizzaeueu.repository

import com.github.pizzaeueu.domain.FlinkQueryResult
import zio.{Ref, Task, URLayer, ZLayer}

trait FlinkQueryRepository {
  def loadJobs: Task[List[FlinkQueryResult]]

  def saveJob(query: FlinkQueryResult): Task[Unit]
}

case class FlinkQueryRepositoryInMemory(ref: Ref[List[FlinkQueryResult]])
    extends FlinkQueryRepository {
  override def loadJobs: Task[List[FlinkQueryResult]] = ref.get

  override def saveJob(query: FlinkQueryResult): Task[Unit] =
    ref.update { allQueries =>
      allQueries :+ query
    }
}

object FlinkQueryRepositoryInMemory {
  def live: URLayer[Ref[List[FlinkQueryResult]], FlinkQueryRepositoryInMemory] =
    ZLayer.fromFunction(FlinkQueryRepositoryInMemory.apply _)
}
