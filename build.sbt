name := "ContactsSyncDemo"
 
version := "1.0" 
      
lazy val `contactssyncdemo` = (project in file(".")).enablePlugins(PlayScala)

      
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"
      
scalaVersion := "2.13.5"
val akkaHttpVersion = "10.2.6"
val akkaVersion = "2.6.17"

libraryDependencies ++= Seq(
  jdbc , ehcache , ws , specs2 % Test , guice ,
  "net.liftweb" %% "lift-json" % "3.5.0",
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
)
      