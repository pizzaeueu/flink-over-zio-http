package com.github.pizzaeueu.domain

trait JobSortOrder {
  val code: String
}

object JobSortOrder {
  case object Asc extends JobSortOrder {
    override val code: String = "asc"
  }
  case object Desc extends JobSortOrder {
    override val code: String = "desc"
  }
}
