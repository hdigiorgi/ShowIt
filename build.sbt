name := """show-photos"""
organization := "com.hdigiorgi"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.6"
scalacOptions += "-language:implicitConversions"

libraryDependencies += guice

// Html
libraryDependencies ++= Seq(
  "org.jsoup" % "jsoup" % "1.11.3",
  "com.atlassian.commonmark" % "commonmark" % "0.11.0",
  "com.github.slugify" % "slugify" % "2.2"
)

// Commons
libraryDependencies ++= Seq(
  "org.apache.commons" % "commons-text" % "1.6",
  "org.apache.commons" % "commons-compress" % "1.18",
  "commons-io" % "commons-io" % "2.6",
  "org.typelevel" %% "cats-core" % "1.4.0",
  "org.im4java" % "im4java" % "1.4.0"
)

// DB
libraryDependencies ++= Seq(
  "org.xerial" % "sqlite-jdbc" % "3.25.2",
  "com.typesafe.play" %% "play-slick" % "3.0.3",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.3",
  "org.jasypt" % "jasypt" % "1.9.2"
)

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test