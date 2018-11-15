import DockerSetup._

name := "magda-indexer"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers += Resolver.bintrayRepo("monsanto", "maven")

libraryDependencies ++= {
  val akkaV       = "2.5.18"
  val akkaHttpV   = "10.0.7"
  val akkaStreamTestKitV  = "2.5.18"
  val akkaTestKitV  = "2.5.18"
  val scalaTestV  = "3.0.1"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-xml" % akkaHttpV,
    "com.typesafe.akka" %% "akka-contrib" % akkaV,
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
    "com.rockymadden.stringmetric" %% "stringmetric-core" % "0.27.4",
    "com.monsanto.labs" %% "mwundo" % "0.1.0" exclude("xerces", "xercesImpl"),
    "org.scalaz" %% "scalaz-core" % "7.2.8",
    "com.typesafe.akka" %% "akka-testkit" % akkaTestKitV % Test,
    "org.scalatest" %% "scalatest" % scalaTestV % "test",
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaStreamTestKitV % Test
  )
}

unmanagedResourceDirectories in Compile ++= {
  if (Option(System.getProperty("excludeMockData")).getOrElse("false").equals("true")) {
    Nil
  } else {
    List(baseDirectory.value / "mock-data")
  }
}

setupDocker( stage)
