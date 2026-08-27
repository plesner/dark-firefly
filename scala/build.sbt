ThisBuild / version := "0.0.1-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.8"
ThisBuild / organization := "tundra"
ThisBuild / javacOptions ++= Seq("-source", "17", "-target", "17")

val scalatestVersion = "3.2.18"

Compile / mainClass := Some("dafi.cli.main")

ThisBuild / scalacOptions ++= Seq(
  "-Xfatal-warnings", "-deprecation"
)

ThisBuild / libraryDependencies += "org.scalatest" %% "scalatest" % scalatestVersion % Test
