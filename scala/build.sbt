val scala3Version = "3.8.4"

lazy val root = project
  .in(file("."))
  .settings(
    name := "dafi",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies += "org.scalameta" %% "munit" % "1.3.4" % Test,
    libraryDependencies += "com.github.scopt" %% "scopt" % "4.0.1",
    libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "2.0.0",
    libraryDependencies += "com.google.flatbuffers" % "flatbuffers-java" % "25.2.10",
  )
