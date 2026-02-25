val scala3Version = "3.8.1"

lazy val root = project
  .in(file("."))
  .settings(
    name                                       := "audit-event-schema-validation",
    version                                    := "0.1.0-SNAPSHOT",
    scalaVersion                               := scala3Version,
    libraryDependencies += "org.playframework" %% "play-json"             % "3.0.6",
    libraryDependencies += "org.scalatest"     %% "scalatest"             % "3.2.19" % Test,
    libraryDependencies += "com.networknt"      % "json-schema-validator" % "3.0.0"
  )
  .settings(Compile / unmanagedResourceDirectories += baseDirectory.value / "cip-schemas" / "conf")
