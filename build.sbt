name := "mesos-scala-client"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "com.twitter" % "finagle-http_2.11" % "6.39.0"

val circeVersion = "0.6.0"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)


PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

libraryDependencies += "com.trueaccord.scalapb" %% "scalapb-json4s" % "0.1.2"
libraryDependencies += "com.google.guava" % "guava" % "20.0"
