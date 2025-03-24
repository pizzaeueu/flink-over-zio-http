package com.github.pizzaeueu.service

import com.github.pizzaeueu.domain.JobSortOrder
import com.github.pizzaeueu.service.mock.{
  FlinkJobRepositoryMock,
  FlinkSqlClientMock
}
import zio.ZIO
import zio.test._

object FlinkSqlServiceSpec extends ZIOSpecDefault {
  override def spec = suite("FlinkSqlService")(
    test("Sort data descending") {
      for {
        flinkService <- ZIO.service[FlinkSqlService]
        jobs <- flinkService.loadJobs(JobSortOrder.Desc)
      } yield assertTrue {
        jobs.head.jobId == "2" && jobs.last.jobId == "1"
      }
    },
    test("Sort data ascending") {
      for {
        flinkService <- ZIO.service[FlinkSqlService]
        jobs <- flinkService.loadJobs(JobSortOrder.Asc)
      } yield assertTrue {
        jobs.head.jobId == "1" && jobs.last.jobId == "2"
      }
    }
  ).provide(
    FlinkJobRepositoryMock.mock,
    FlinkSqlClientMock.mock,
    FlinkSqlService.live
  )
}
