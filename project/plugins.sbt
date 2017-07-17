logLevel := Level.Warn

resolvers += Resolver.url( "bintray-csl-sbt-plugins",
  url("https://bintray.com/twittercsl/sbt-plugins"))( Resolver.ivyStylePatterns)

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  "Twitter Maven" at "https://maven.twttr.com"
)

addSbtPlugin("com.twitter" % "scrooge-sbt-plugin" % "4.12.0")

