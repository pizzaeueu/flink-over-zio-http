package com.github.pizzaeueu.domain

case class StatementStatus(status: String)

object StatementStatus {
  val Finished = StatementStatus("FINISHED")
  val Running = StatementStatus("RUNNING")
  val Pending = StatementStatus("PENDING")
}
