val ScalaVer = "2.12.1"

val Cats          = "0.8.1"
val Shapeless     = "2.3.2"
val Scalacheck    = "1.13.4"
val KindProjector = "0.9.3"

val FS2   = "0.9.2"
val Jsoup = "1.10.2"
val Circe = "0.6.1"

val ScalacheckMinTests = 1000

lazy val commonSettings = Seq(
  name    := "analysis"
, version := "0.1.0"
, scalaVersion := ScalaVer
, libraryDependencies ++= Seq(
    "org.typelevel"  %% "cats"       % Cats
  , "com.chuusai"    %% "shapeless"  % Shapeless
  , "org.scalacheck" %% "scalacheck" % Scalacheck  % "test"
  
  , "org.jsoup" %  "jsoup"      % Jsoup
  , "co.fs2"    %% "fs2-core"   % FS2
  , "io.circe"  %% "circe-core" % Circe
  )
, addCompilerPlugin("org.spire-math" %% "kind-projector" % KindProjector)
, scalacOptions ++= Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:experimental.macros",
      "-unchecked",
      // "-Xfatal-warnings",
      "-Xlint",
      // "-Yinline-warnings",
      "-Ywarn-dead-code",
      "-Xfuture")
, testOptions in Test += Tests.Argument(TestFrameworks.ScalaCheck, "-minSuccessfulTests", ScalacheckMinTests.toString, "-workers", "10", "-verbosity", "1")
)

lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(
    initialCommands := "import analysis._; import Main._"
  )
