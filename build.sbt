name := "distributed-chat-system"

version := "0.1"

val AkkaVersion = "2.6.17"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-remote" % AkkaVersion,
  "com.typesafe.akka" %% "akka-cluster-typed" % AkkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.scalafx" %% "scalafx" % "8.0.192-R14",
  "org.scalafx" %% "scalafxml-core-sfx8" % "0.5"
  
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

fork := true
