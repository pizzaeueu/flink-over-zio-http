package com.github.pizzaeueu.domain

import zio.json.ast.Json

case class FlinkQueryResult(jobId: String, result: Json)


