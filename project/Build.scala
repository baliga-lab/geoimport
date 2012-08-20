import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "geoimport-ui"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "mysql" % "mysql-connector-java" % "5.1.18",
      "org.scalaquery" % "scalaquery_2.9.1" % "0.10.0-M1"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    )

}
