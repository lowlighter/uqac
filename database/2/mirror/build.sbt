name := "bddtp2"

version := "0.1"

scalaVersion := "2.12.8"

lazy val akkaVersion = "2.5.22"
lazy val akkaHttpVersion = "10.0.9"
lazy val circeVersion = "0.10.0"

libraryDependencies ++= Seq(
  //Spark
  "org.apache.spark" %% "spark-core" % "2.4.0",
  "org.apache.spark" % "spark-sql_2.12" % "2.4.0",
  //Akka
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  //Circe
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
)

enablePlugins(JavaAppPackaging)
