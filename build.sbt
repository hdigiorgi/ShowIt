import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}


lazy val sharedSettings = Seq(
  name := """show-photos""",
  organization := "com.hdigiorgi",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.12.7",
  scalacOptions += "-language:implicitConversions"
)

lazy val server = (project in file("server")).settings(sharedSettings).settings(
  libraryDependencies += guice,

  // Html
  libraryDependencies ++= Seq(
    "org.jsoup" % "jsoup" % "1.11.3",
    "com.atlassian.commonmark" % "commonmark" % "0.11.0",
    "com.github.slugify" % "slugify" % "2.2"
  ),

  // Commons
  libraryDependencies ++= Seq(
    "org.apache.commons" % "commons-text" % "1.6",
    "commons-io" % "commons-io" % "2.6",
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

  //js
  resources in Compile += (fullOptJS in Compile in client).value.data,
  //unmanagedResources in Compile ++= (file("../client/src/main/scala/com/hdigiorgi/showit/") ** "*.scala").get
  unmanagedSourceDirectories in Compile += file("../client/src/main/scala/com/hdigiorgi/showit/"),

).enablePlugins(PlayScala).dependsOn(appJVM).dependsOn(client)

lazy val client = (project in file("client")).settings(sharedSettings).settings(
  artifactPath in(Compile, fastOptJS)             := baseDirectory.value / ".." / "server" / "public" / "scalajs" / "app.js",
  artifactPath in(Compile, fullOptJS)             := baseDirectory.value / ".." / "server" / "public" / "scalajs" / "app.js",
  artifactPath in(Compile, packageJSDependencies) := baseDirectory.value / ".." / "server" / "public" / "scalajs" / "dependency.js"
).enablePlugins(ScalaJSPlugin).dependsOn(appJS)

lazy val app = crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
lazy val appJS = app.js
lazy val appJVM = app.jvm



