name := "gander"

version := "0.0.2-SNAPSHOT"

organization := "com.beachape"

description := "Html Content / Article Extractor in Scala"

scalacOptions ++= Seq("-unchecked", "-deprecation")

libraryDependencies ++= Seq(
  // Main dependencies
  "org.jsoup"              % "jsoup"         % "1.10.2",
  "commons-io"             % "commons-io"    % "2.5",
  "org.apache.commons"     % "commons-lang3" % "3.5",
  "com.github.nscala-time" %% "nscala-time"  % "2.16.0",
  // Testing dependencies
  "com.novocode" % "junit-interface" % "0.11"   % Test,
  "org.slf4j"    % "slf4j-log4j12"   % "1.7.22" % Test,
  // Build dependencies
  "org.slf4j" % "slf4j-api" % "1.7.22" % Compile
)

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

organization := "com.beachape"

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ =>
  false
}

scalacOptions in (Compile, compile) ++= {
  val base = Seq(
    "-Xlog-free-terms",
    "-encoding",
    "UTF-8", // yes, this is 2 args
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code", // N.B. doesn't work well with the ??? hole
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-deprecation",
    "-Xfuture"
  )
  if (scalaVersion.value.startsWith("2.10"))
    base
  else
    base :+ "-Ywarn-unused-import"

}

pomExtra := (
  <url>https://github.com/lloydmeta/gander</url>
    <licenses>
      <license>
        <name>MIT</name>
        <url>http://opensource.org/licenses/MIT</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:lloydmeta/gander.git</url>
      <connection>scm:git:git@github.com:lloydmeta/gander.git</connection>
    </scm>
    <developers>
      <developer>
        <id>lloydmeta</id>
        <name>Lloyd Chan</name>
        <url>https://beachape.com</url>
      </developer>
    </developers>
)

reformatOnCompileSettings

scalafmtConfig := Some(file(".scalafmt.conf"))
