ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.16"

lazy val root = (project in file("."))
  .settings(
    name := "flink-over-zio-http"
  )
  .settings(
    libraryDependencies ++= ZIO.all ++ Logging.all ++ Testing.all
  )
  .settings(
    assembly / assemblyMergeStrategy := (_ => MergeStrategy.first)
  )
  .settings(
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )

addCommandAlias("fmt", "scalafmtSbt; scalafmtAll;")
