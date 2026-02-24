val scala3Version = "3.8.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "audit-event-schema-validation",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies += "org.scalameta" %% "munit" % "1.2.2" % Test,
    libraryDependencies += "org.playframework" %% "play-json" % "3.0.6",
    libraryDependencies += "com.github.erosb" % "json-sKema" % "0.28.0",
    libraryDependencies += "org.scala-lang" %% "scala3-staging" % "3.8.1",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test
  )
