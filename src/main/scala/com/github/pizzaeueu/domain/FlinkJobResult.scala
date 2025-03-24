package com.github.pizzaeueu.domain

import zio.json.ast.Json

case class FlinkJobResult(jobId: String, result: Json)


