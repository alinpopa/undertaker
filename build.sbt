lazy val commonSettings = Seq(
  organization := "undertaker",
  version := "1.0.0",
  scalaVersion := "2.11.8",
  scalacOptions := Seq("-unchecked", "-deprecation", "-feature")
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "undertaker",
    mainClass in assembly := Some("undertaker.Main"),
    assemblyJarName in assembly := s"${name.value}-${version.value}.jar"
  )

libraryDependencies ++= {
  val akkaVersion = "2.4.8"
  Seq(
    "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion,
    "org.scalatest" %% "scalatest" % "2.2.6" % "test",
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % "test"
  )
}
