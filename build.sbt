import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}
import complete.DefaultParsers._
import sbt.internal.inc.classpath.ClasspathUtilities


lazy val sharedSettings = Seq(
  name := """show-photos""",
  organization := "com.hdigiorgi",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.12.7",
  scalacOptions += "-language:implicitConversions",
)

lazy val server = (project in file("server")).settings(sharedSettings).settings(
  libraryDependencies += guice,

  // Html, CSV, Markdown
  libraryDependencies ++= Seq(
    "org.jsoup" % "jsoup" % "1.11.3",
    "com.atlassian.commonmark" % "commonmark" % "0.11.0",
    "com.github.slugify" % "slugify" % "2.2",
    "org.apache.commons" % "commons-csv" % "1.6"
  ),


  // Commons
  libraryDependencies ++= Seq(
    "org.apache.commons" % "commons-text" % "1.6",
    "commons-validator" % "commons-validator" % "1.4.0",
    "commons-io" % "commons-io" % "2.6",
    "org.apache.httpcomponents" % "httpclient" % "4.5.6",
    "org.im4java" % "im4java" % "1.4.0"
  ),

  // DB
  libraryDependencies ++= Seq(
    "org.xerial" % "sqlite-jdbc" % "3.25.2",
    "com.typesafe.play" %% "play-slick" % "3.0.3",
    "com.typesafe.play" %% "play-slick-evolutions" % "3.0.3",
    "org.jasypt" % "jasypt" % "1.9.2",
    "net.lingala.zip4j" % "zip4j" % "1.3.2"
  ),

  // Language
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % "1.4.0",
    "com.jsuereth" %% "scala-arm" % "2.0"
  ),

  // Test
  libraryDependencies ++= Seq(
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
    "org.scalactic" %% "scalactic" % "3.0.5",
    "org.scalatest" %% "scalatest" % "3.0.5" % "test",
    "com.github.javafaker" % "javafaker" % "0.16",
    "org.scalawebtest" %% "scalawebtest-core" % "2.0.1" % "test",
    "org.seleniumhq.selenium" % "selenium-java" % "3.6.0" % "test",
    "org.seleniumhq.selenium" % "htmlunit-driver" % "2.27" % "test"
  ),
  parallelExecution in Test := false,
  logBuffered in Test := false,

  // Observability
  libraryDependencies ++= Seq(
    "org.apache.logging.log4j" % "log4j-core" % "2.11.1",
    "org.apache.logging.log4j" % "log4j-api" % "2.11.1",
    "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.11.1",
    "com.lmax" % "disruptor" % "3.4.2" // async logging
  ),
  javaOptions += "-DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector", // async logging

  //js
  resources in Compile += (fullOptJS in Compile in client).value.data,
  unmanagedSourceDirectories in Compile += file("../client/src/main/scala/com/hdigiorgi/showit/"),

).enablePlugins(PlayScala).disablePlugins(PlayLogback).dependsOn(client).settings( // disable logback for custom log4j2
  excludeDependencies  += "ch.qos.logback" % "*"
)

lazy val client = (project in file("client")).settings(sharedSettings).settings(
  libraryDependencies += "be.doeraene" %%% "scalajs-jquery" % "0.9.4",
  libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.6",
  scalaJSUseMainModuleInitializer := true,
  (mainClass in Compile) := Some("com.hdigiorgi.showit.Main"),
  artifactPath in(Compile, fastOptJS)             := baseDirectory.value / ".." / "server" / "public" / "scalajs" / "app.js",
  artifactPath in(Compile, fullOptJS)             := baseDirectory.value / ".." / "server" / "public" / "scalajs" / "app.js",
  artifactPath in(Compile, packageJSDependencies) := baseDirectory.value / ".." / "server" / "public" / "scalajs" / "dependency.js",
  scalacOptions += "-P:scalajs:sjsDefinedByDefault"
).enablePlugins(ScalaJSPlugin)

run := (Keys.run in Compile in server).evaluated

val action = inputKey[Unit]("run actions from the Main class")
action := {
  val args: Array[String] = spaceDelimited("args").parsed.toArray
  val classPath = (fullClasspath in Runtime in server).value
  val loader: ClassLoader = ClasspathUtilities.toLoader(classPath.map(_.data).map(_.getAbsoluteFile))
  val method = loader.loadClass("com.hdigiorgi.showPhoto.application.Main").getMethod("main", classOf[Array[String]])
  method.invoke(null, args)
}
action := action.dependsOn(compile in Compile in server).evaluated
