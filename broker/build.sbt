ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "broker"
  )


val AkkaVersion = "2.6.19"
val AkkaHttpVersion = "10.1.11"
libraryDependencies ++= Seq(
  "com.lightbend.akka" %% "akka-stream-alpakka-sse" % "2.0.2",
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.lihaoyi" %% "upickle" % "0.9.5",
  "org.mongodb.scala" %% "mongo-scala-driver" % "4.2.2",
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
//  "com.typesafe.akka" % "akka-actor-typed_2.12" % "2.6.8",
  "net.liftweb" %% "lift-json" % "3.4.3",
  "org.json4s" %% "json4s-jackson" % "3.7.0-M11",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % Runtime,
  "com.typesafe.play" %% "play-json" % "2.9.2",
//  "com.elkozmon" % "akka-stream-firebase-queue_2.12" % "2.1"
//  "com.google.firebase" % "firebase-server-sdk" % "3.0.1"
"com.google.firebase" % "firebase-server-sdk" % "3.0.3",
"com.twitter" %% "finagle-http" % "22.4.0"
)
//libraryDependencies += "com.typesafe.akka" % "akka-actor-typed_2.12" % "2.6.8",
//libraryDependencies += "com.typesafe.akka" % "akka-actor-testkit-typed_2.12" % "2.6.8" % Test