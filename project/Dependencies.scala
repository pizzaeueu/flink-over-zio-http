import sbt.*

object VersionOf {
  val zio = "2.1.16"
  val `zio-http` = "3.1.0"
  val `zio-json` = "0.7.39"
  val `slf4j-api` = "2.0.17"
  val logback = "1.5.18"

}
object ZIO {
  val zioCore = "dev.zio" %% "zio" % VersionOf.zio
  val zioStreams = "dev.zio" %% "zio-streams" % VersionOf.zio
  val zioJson = "dev.zio" %% "zio-json" % VersionOf.`zio-json`
  val zioHttp = "dev.zio" %% "zio-http" % VersionOf.`zio-http`

  val all: Seq[ModuleID] = Seq(zioCore, zioStreams, zioJson, zioHttp)
}

object Logging {
  val slf4jApi = "org.slf4j" % "slf4j-api" % VersionOf.`slf4j-api`
  val logback = "ch.qos.logback" % "logback-classic" % "1.5.18"

  val all: Seq[ModuleID] = Seq(slf4jApi, logback)
}
