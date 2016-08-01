An actor based REST api workflows manager

#### How to

##### Prerequisites

- [JVM](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) (>= 1.8)
- [SBT](http://www.scala-sbt.org/download.html) (>= 0.13.x)

##### Config

- configs are placed within `src/main/resources/application.conf`

##### Tests

- `sbt test`

##### Run

- `sbt run`
- or
- `sbt assembly && java -jar target/scala-2.11/undertaker-1.0.0.jar`
