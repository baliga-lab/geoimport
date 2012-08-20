package models
import play.api.Play.current
import play.api.db._

import scala.collection.mutable.{ArrayBuffer}

import org.scalaquery.ql._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.{ExtendedTable => Table}
import org.scalaquery.ql.extended.MySQLDriver.Implicit._
import org.scalaquery.session.{Database, Session}

// This queries object provides the geoimport package queries
// interface with an implicit database session from Play
object Queries {
  lazy val database = Database.forDataSource(DB.getDataSource())

  def allConfigs = database.withSession { implicit db: Session =>
    GeoImportQueries.allConfigs
  }

  private def updateConfig(id: Long, name: String, query: String,
                   idcols: Seq[String]) = database.withSession { implicit db: Session =>
    println("update in database")
    val q = for (c <- ImportConfigs if c.id === id) yield c.name ~ c.query
    q.update(name, query)
    val q1 = IdColumns.where(_.configId === id)
    q1.delete
    for (i <- 0 until idcols.length) {
      IdColumns.configId ~ IdColumns.name ~ IdColumns.rank insert(id, idcols(i), i + 1)
    }
  }

  private def insertConfig(id: Long, name: String, query: String,
                   idcols: Seq[String]) = database.withSession { implicit db: Session =>
    println("insert in database")
    ImportConfigs.name ~ ImportConfigs.query insert(name, query)
    // this only works with MySQL !!!
    val seqID = SimpleFunction.nullary[Int]("LAST_INSERT_ID")
    val importConfigId: Long = Query(seqID).first

    // insert the attached id columns
    for (i <- 0 until idcols.length) {
      IdColumns.configId ~ IdColumns.name ~ IdColumns.rank insert(importConfigId, idcols(i), i + 1)
    }
  }

  def saveConfig(id: Long, name: String, query: String,
                 idcols: Seq[String]) = {
    if (id > 0) updateConfig(id, name, query, idcols)
    else insertConfig(id, name, query, idcols)
  }

  def deleteConfig(id: Long) = database.withSession { implicit db: Session =>
    val q1 = IdColumns.where(_.configId === id)
    val q2 = ImportConfigs.where(_.id === id)
    q1.delete
    q2.delete
  }
}
