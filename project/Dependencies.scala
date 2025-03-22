import sbt.*

object VersionOf {
  val zio = "2.1.16"
  val `zio-http` = "3.1.0"
  val `zio-json` = "0.7.39"
  val `slf4j-api` = "2.0.17"
  val logback = "1.5.18"
  val `zio-config` = "4.0.4"
  val `zio-schema` = "1.6.6"

}
object ZIO {
  val zioCore = "dev.zio" %% "zio" % VersionOf.zio
  val zioJson = "dev.zio" %% "zio-json" % VersionOf.`zio-json`
  val zioHttp = "dev.zio" %% "zio-http" % VersionOf.`zio-http`
  val zioConfig = "dev.zio" %% "zio-config" % VersionOf.`zio-config`
  val zioConfigMagnolia =
    "dev.zio" %% "zio-config-magnolia" % VersionOf.`zio-config`
  val zioConfigTypesafe =
    "dev.zio" %% "zio-config-typesafe" % VersionOf.`zio-config`
  val zioSchema = "dev.zio" %% "zio-schema" % VersionOf.`zio-schema`

  val all: Seq[ModuleID] = Seq(
    zioCore,
    zioJson,
    zioHttp,
    zioConfig,
    zioConfigMagnolia,
    zioConfigTypesafe,
    zioSchema
  )
}

object Logging {
  val slf4jApi = "org.slf4j" % "slf4j-api" % VersionOf.`slf4j-api`
  val logback = "ch.qos.logback" % "logback-classic" % VersionOf.logback

  val all: Seq[ModuleID] = Seq(slf4jApi, logback)
}
