import sbt._
import Keys._
import play.Project._
 
object ApplicationBuild extends Build {
 
  val appName         = "FishStore"
  val appVersion      = "1.0"
 
  val appDependencies = Seq(
      "com.typesafe.akka" %% "akka-testkit" % "2.2.1" % "test",
      "joda-time" % "joda-time" % "2.3",
      "org.reactivemongo" %% "reactivemongo" % "0.10.0",
      "org.mongodb" %% "casbah" % "2.5.0",
      cache
  )
 
  val main = play.Project(appName, appVersion, appDependencies).settings(
    scalaVersion := "2.10.2"
  )
 
}