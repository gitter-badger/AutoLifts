import AutoLift._
import com.typesafe.sbt.SbtSite.SiteKeys._
import com.typesafe.sbt.SbtGhPages.GhPagesKeys._
import sbtunidoc.Plugin.UnidocKeys._

//todo, rename folder autolift-core
lazy val autoz = build("autolift-core", "autoz").settings(
  libraryDependencies ++= Seq(
    "org.scalaz" %% "scalaz-core" % ScalaZ,
    "org.typelevel" %% "export-hook" % "1.1.0",
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    compilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full),
    "org.scalatest" %% "scalatest" % "2.2.1" % "test"
  ),
  sonatypeProfileName := "wheaties"
)

lazy val autoAlge = build("autolift-algebird", "auto-algebird").settings(
  libraryDependencies ++= Seq(
    "org.typelevel" %% "export-hook" % "1.1.0",
    compilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full),
    "com.twitter" %% "algebird-core" % "0.11.0",
    "com.twitter" %% "algebird-util" % "0.11.0",
    "com.twitter" %% "algebird-test" % "0.11.0" % "test", //check if actually needed
    "org.scalatest" %% "scalatest" % "2.2.1" % "test"
  ),
  sonatypeProfileName := "wheaties"
)
.dependsOn(autoz)

lazy val autoScalaz = build("autolift-scalaz", "autolift-scalaz").settings(
  libraryDependencies ++= Seq(
    "org.scalaz" %% "scalaz-core" % ScalaZ,
    "org.typelevel" %% "export-hook" % "1.1.0",
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    compilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full),
    "org.scalatest" %% "scalatest" % "2.2.1" % "test"
  ),
  sonatypeProfileName := "wheaties"
)
.dependsOn(autoz)

lazy val docs = build("docs", "docs")
  .settings(tutSettings: _*)
  .settings(site.settings: _*)
  .settings(ghpages.settings: _*)
  .settings(
    publishArtifact := false,
    site.addMappingsToSiteDir(tut, "_tut"),
    site.addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc), "latest/api"),
    unidocProjectFilter in (ScalaUnidoc, unidoc) := inAnyProject,
    ghpagesNoJekyll := false,
    git.remoteRepo := "git@github.com:wheaties/autolifts.git",
    autoAPIMappings := true,
    includeFilter in makeSite := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.js" | "*.md"
  )
  .settings(site.includeScaladoc(): _*)
  .settings(site.jekyllSupport(): _*)
  .settings(unidocSettings: _*)
  .dependsOn(autoz, autoAlge)

lazy val bench = build("bench", "bench")
  .settings(
    publishArtifact := false
  )
  .dependsOn(autoz, autoScalaz)
  .enablePlugins(JmhPlugin)


scalaVersion := AutoLift.ScalaVersion

addCommandAlias("gen-site", "unidoc;tut;make-site")