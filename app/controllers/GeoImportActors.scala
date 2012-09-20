package controllers

import java.io._

import akka.actor._
import play.api.db._
import org.scalaquery.session.{Database, Session}
import play.api.Play.current

import models._

// **************************************************
// These actors define the import functionality
// that runs asynchronously to the web application
// **************************************************

class GeoImportActor extends Actor {
  def receive = {
    case StartImport =>
      lazy val database = Database.forDataSource(DB.getDataSource())
      val configs = database.withSession { implicit db: Session =>
        GeoImportQueries.allConfigs
      }
      println("CONFIGS: " + configs)
      val cachedir = new File("cache")
      val outputdir = new File("output")
      if (!cachedir.exists) cachedir.mkdir
      if (!outputdir.exists) outputdir.mkdir
      configs.foreach { config => GeoImport.mergeOrganism(cachedir, outputdir, config) }
      sender ! ImportFinished
    case _ =>
      println("not handled")
  }
  override def postStop {
    println("GeoImportActor was stopped")
  }
}

// This is the supervisor actor for our import actor
sealed trait GeoImportStatus
case object Stopped extends GeoImportStatus
case object Running extends GeoImportStatus
case object Crashed extends GeoImportStatus

sealed trait GeoImportMessage
case object ImportStatus extends GeoImportMessage
case object StartImport extends GeoImportMessage
case object ImportFinished extends GeoImportMessage

class GeoImportManager extends Actor {
  var status: GeoImportStatus = Stopped
  val child = context.actorOf(Props[GeoImportActor], "geoimport-worker")
  context.watch(child)

  def receive = {
    case ImportStatus =>
      sender ! status
    case StartImport =>
      if (status == Stopped) child  ! StartImport
      status = Running
    case ImportFinished =>
      status = Stopped
    case Terminated(child) =>
      status = Crashed
    case _ =>
      println("some message")
      //context.stop(child)
  }
  override def postStop {
    println("GeoImportManager was stopped")
  }
}
