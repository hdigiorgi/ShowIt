name := """show-photos"""
organization := "com.hdigiorgi"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.6"
scalacOptions += "-language:implicitConversions"

libraryDependencies += guice
libraryDependencies += "org.typelevel" %% "cats-core" % "1.4.0"
libraryDependencies += "org.jasypt" % "jasypt" % "1.9.2"
libraryDependencies += "com.github.slugify" % "slugify" % "2.2"
libraryDependencies += "org.jsoup" % "jsoup" % "1.11.3"
libraryDependencies += "org.apache.commons" % "commons-text" % "1.6"
libraryDependencies += "com.atlassian.commonmark" % "commonmark" % "0.11.0"
libraryDependencies += "commons-io" % "commons-io" % "2.6"
libraryDependencies += "org.im4java" % "im4java" % "1.4.0"
libraryDependencies ++= Seq(
  "org.xerial" % "sqlite-jdbc" % "3.25.2",
  "com.typesafe.play" %% "play-slick" % "3.0.3",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.3"
)
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test