lazy val commonSettings = Seq(
  organization := "undertaker",
  version := "1.0.0",
  scalaVersion := "2.12.4",
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
  Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.5.14",
    "com.typesafe.akka" %% "akka-stream" % "2.5.14",
    "com.typesafe.akka" %% "akka-http" % "10.1.3",
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.3",
    "com.github.kxbmap" %% "configs" % "0.4.4",
    "org.scalatest" %% "scalatest" % "3.0.5" % "test",
    "com.typesafe.akka" %% "akka-testkit" % "2.5.14" % "test",
    "org.scalamock" %% "scalamock" % "4.1.0" % "test"
  )
}
