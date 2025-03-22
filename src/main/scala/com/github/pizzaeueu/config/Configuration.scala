package com.github.pizzaeueu.config

import zio.Config
import zio.config.magnolia._
import zio.config.typesafe.TypesafeConfigProvider

object Configuration {
  private implicit val appConfig: Config[AppConfig] = deriveConfig[AppConfig]

  val provider = TypesafeConfigProvider.fromResourcePath().load[AppConfig]
}
