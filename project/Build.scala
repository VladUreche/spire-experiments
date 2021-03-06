import sbt._
import Keys._
import Process._

object MiniboxingBuild extends Build {

  // http://stackoverflow.com/questions/6506377/how-to-get-list-of-dependency-jars-from-an-sbt-0-10-0-project
  val getJars = TaskKey[Unit]("get-jars")
  val getJarsTask = getJars <<= (target, fullClasspath in Runtime) map { (target, cp) =>
    println("Target path is: "+target)
    println("Full classpath is: "+cp.map(_.data).mkString(":"))
  }

  val defaults = Defaults.defaultSettings ++ Seq(
    scalaSource in Compile := baseDirectory.value / "src",
    javaSource in Compile := baseDirectory.value / "src",
    scalaSource in Test := baseDirectory.value / "test",
    javaSource in Test := baseDirectory.value / "test",

    unmanagedSourceDirectories in Compile := Seq((scalaSource in Compile).value),
    unmanagedSourceDirectories in Test := Seq((scalaSource in Test).value),
    //http://stackoverflow.com/questions/10472840/how-to-attach-sources-to-sbt-managed-dependencies-in-scala-ide#answer-11683728
    com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys.withSource := true,

    resolvers in ThisBuild ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("snapshots")
    ),

    scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-Xlint"),
    scalacOptions ++= Seq("-optimize", "-Yinline-warnings"),

    scalaVersion := "2.10.4",

    libraryDependencies += "org.spire-math" %% "spire" % "0.7.4"
  )

  val scalaMeter = {
    val sMeter  = Seq("com.github.axel22" %% "scalameter" % "0.4")
    Seq(
      libraryDependencies ++= sMeter, 
      testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework")
    )
  }

  val junitDeps: Seq[Setting[_]] = Seq(
    libraryDependencies ++= Seq(
      "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
      "com.novocode" % "junit-interface" % "0.10-M2" % "test"
    ),
    parallelExecution in Test := false,
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")
  )

  lazy val _mboxing    = Project(id = "miniboxing",             base = file("."),                      settings = defaults) aggregate (example)
  lazy val example     = Project(id = "miniboxing-example",     base = file("components/example"),     settings = defaults ++ scalaMeter ++ junitDeps)
}
