package com.github.pizzaeueu.config

final case class AppConfig(
    flink: FlinkConfig,
    server: HttpServerConfig
)
