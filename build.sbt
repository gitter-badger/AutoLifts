import AutoLift._
import com.typesafe.sbt.SbtSite.SiteKeys._
import com.typesafe.sbt.SbtGhPages.GhPagesKeys._

lazy val autoz = build("autolift", "autoz").settings(
  libraryDependencies ++= Seq(
    "org.scalaz" %% "scalaz-core" % ScalaZ,
    "org.scalatest" %% "scalatest" % "2.2.1" % "test"),
  sonatypeProfileName := "wheaties",
  wartremoverErrors in (Compile, compile) ++= Warts.allBut(Wart.Var, Wart.NoNeedForMonad)
)

lazy val docs = build("docs", "docs")
  .settings(tutSettings: _*)
  .settings(site.settings: _*)
  .settings(ghpages.settings: _*)
  .settings(
    publishArtifact := false,
    site.addMappingsToSiteDir(tut, "_tut"),
    ghpagesNoJekyll := false,
    git.remoteRepo := "git@github.com:wheaties/autolifts.git"
  )
  .dependsOn(autoz)

scalaVersion := AutoLift.ScalaVersion
site.includeScaladoc()