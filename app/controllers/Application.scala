package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models._
import org.systemsbiology.services.eutils._

object Application extends Controller {

  val saveConfigForm = Form(
    tuple("id" -> optional(longNumber),
          "name" -> nonEmptyText,
          "query" -> nonEmptyText,
          "idcolumns" -> optional(text))
  )

  val deleteForm = Form("id" -> longNumber)

  private def getSummaries(query: String) = {
    val searchresult = ESearch.get(GEO.DataSets, query)
    val webEnv = (searchresult \ "WebEnv").text
    val queryKey = (searchresult \ "QueryKey").text
    ESummary.getFromPreviousSearch(GEO.DataSets,
                                   webEnv, queryKey)
  }

  private def getPlatforms(organism: String) = {
    val query = "%s".format(organism)
    val summary = getSummaries(organism)
    (summary \\ "Item").filter {
      item => (item \ "@Name").text == "GPL"
    }.map { item => item.text.split(";") }.flatten.toSet.toSeq
  }
  
  def index = Action {
    Ok(views.html.index(Queries.allConfigs, null))
  }

  def configs = Action {
    Ok(views.html.configlist(Queries.allConfigs))
  }

  def deleteConfig = Action { implicit request =>
    deleteForm.bindFromRequest.fold(
      errors => {
        println("BAD REQUEST: " + errors)
        //BadRequest(views.html.index(Queries.allConfigs))
        Redirect(routes.Application.index)
      },
      id => {
        println("Deleting (TODO): " + id)
        Queries.deleteConfig(id)
        // handle and redirect to avoid double POST
        Redirect(routes.Application.index)
      }
    )
  }

  def saveConfig = Action { implicit request =>
    saveConfigForm.bindFromRequest.fold(
      errors => {
        println("BAD REQUEST !!!!: " + errors)
        BadRequest(views.html.index(Queries.allConfigs, errors))
        //Redirect(routes.Application.index)
      },
      params => {
        val (id, name, query, idcols) = params
        val configId: Long = id.getOrElse(0)
        val idColumns = idcols.getOrElse("").split(",").map(_.trim).toSeq
        // println("SAVING: " + configId + "cols = [" + idColumns + "]")
        Queries.saveConfig(configId, name, query, idColumns)
        Redirect(routes.Application.index)
      }
    )
  }

  def platforms(query: String) = Action {
    val urls = getPlatforms(query).map(a => GEOFTPURLBuilder.urlSOFTByPlatform(a))
    Ok(views.html.platforms(query, urls))
  }
}
