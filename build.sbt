name := "Goose"

version := "2.1.25-SNAPSHOT"

organization := "com.gravity"

organizationHomepage := Some(url("http://gravity.com/"))

homepage := Some(url("https://github.com/warrd/goose-fork"))

description := "Html Content / Article Extractor in Scala"

licenses += "Apache2" -> url("http://www.apache.org/licenses/")

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-unchecked", "-deprecation")

libraryDependencies ++= Seq(
  // Main dependencies
  "org.slf4j"                 % "slf4j-simple"  % "1.7.22",
  "org.jsoup"                 % "jsoup"         % "1.10.2",
  "commons-io"                % "commons-io"    % "2.5",
  "org.apache.httpcomponents" % "httpclient"    % "4.5.2",
  "org.apache.commons"        % "commons-lang3" % "3.5",
  "com.github.nscala-time"    %% "nscala-time"  % "2.16.0",
  // Testing dependencies
  "com.novocode" % "junit-interface" % "0.11"   % Test,
  "org.slf4j"    % "slf4j-log4j12"   % "1.7.22" % Test,
  // Build dependencies
  "org.slf4j" % "slf4j-api" % "1.7.22" % Compile
)

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := (
  <scm>
    <url>git@github.com:lloydmeta/gander.git</url>
    <connection>scm:git:git@github.com:lloydmeta/gander.git</connection>
  </scm>
)
