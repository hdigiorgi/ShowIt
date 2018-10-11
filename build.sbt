name := """show-photos"""
organization := "com.hdigiorgi"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.6"
scalacOptions += "-language:implicitConversions"

libraryDependencies += guice
libraryDependencies += "org.typelevel" %% "cats-core" % "1.4.0"
libraryDependencies ++= Seq(
  "org.xerial" % "sqlite-jdbc" % "3.25.2",
  "com.typesafe.play" %% "play-slick" % "3.0.3",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.3"
)
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test