name := "mesos-scala-client"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "com.twitter" % "finagle-http_2.11" % "6.40.0"


val circeVersion = "0.6.0"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

// Exclusion rule required to evict the com.twitter libthrift library.
libraryDependencies ++= Seq(
  "org.apache.thrift" % "libthrift" % "0.9.3",
  "com.twitter" %% "scrooge-core" % "4.12.0" exclude("com.twitter", "libthrift"),
  "com.twitter" %% "finagle-thrift" % "6.40.0" exclude("com.twitter", "libthrift")
)

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

scroogeThriftOutputFolder in Compile <<= baseDirectory(_ / "target" / "scala-2.11"
  / "src_managed")

libraryDependencies += "com.trueaccord.scalapb" %% "scalapb-json4s" % "0.1.2"
libraryDependencies += "com.google.guava" % "guava" % "20.0"
libraryDependencies += "com.twitter" %% "finatra-thrift" % "2.6.0"
libraryDependencies += "com.twitter" %% "finatra-com.treadstone90.mesos.http" % "2.6.0"
libraryDependencies +=  "com.twitter" %% "finatra-thrift" % "2.6.0" % "test" classifier "tests"

// https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.7"

// https://mvnrepository.com/artifact/org.apache.curator/curator-framework
libraryDependencies += "org.apache.curator" % "curator-framework" % "3.3.0"

libraryDependencies += "org.apache.curator" % "curator-recipes" % "3.3.0"



