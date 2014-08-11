package controllers

import com.p44.db.store.two.MongoDbStoreTwo
import com.p44.models.{WhaleSighting, WhaleSightingNew}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContent, Request, Action, Controller}
import reactivemongo.api.DefaultDB

import scala.concurrent.{Future, ExecutionContext}

/**
 * Created by markwilson on 8/9/14.
 */
object WhaleSightingController extends Controller {

  val db: DefaultDB = MongoDbStoreTwo.STORE_TWO_DB

  implicit val ec = ExecutionContext.global

  /** POST */
  def postWhaleSighting = Action.async { request =>
    val fSighting = Future[Option[WhaleSightingNew]] {
      resolveNewWhaleSightingJsonToObj(request)
    }
    fSighting.flatMap { ows =>
      ows match {
        case None => Future.successful(BadRequest("Please check your request for content type of json as well as the json format."))
        case Some(ws) => {
          val fResult = WhaleSightingNew.insertOneAsFuture(db, ws)
          fResult.map { le =>
            le.ok match {
              case true => Ok("Saved")
              case _ => InternalServerError("Failed to save.")
            }
          }
        }
      }
    }
  }

  /**
   * GET
   *
   * @param id Mongo _id hex string
   */
  def getWhaleSighting(id: String) = Action.async { request =>
    val fSighting: Future[Option[WhaleSighting]] = WhaleSighting.findOneByQueryAsFuture(db, WhaleSighting.findById(id))
    fSighting.map { ows =>
      ows match {
        case None => BadRequest("None found for id " + id)
        case Some(ws) => Ok(Json.prettyPrint(Json.toJson(ws)))
      }
    }
  }

  /**
   * PUT - add a comment
   *
   * @param id Mongo _id hex string
   */
  def putWhaleSightingAddComment(id: String) = Action.async { request =>
    // TODO
    Future.successful(NotImplemented("Not Implemented"))
  }

  /**
   * REMOVE
   *
   * @param id
   */
  def removeWhaleSighting(id: String) = Action.async { request =>
    val fLastError = WhaleSighting.getCollection(db).remove(WhaleSighting.findById(id), WhaleSighting.lastErrorDefault, true)
    fLastError.map { le =>
      le.ok match {
        case false => BadRequest("None found or failed to remove for id " + id)
        case _ => Ok("Removed " + id)
      }
    }
  }

  /** POST Json to WhaleSightingNew */
  def resolveNewWhaleSightingJsonToObj(request: Request[AnyContent]): Option[WhaleSightingNew] = {
    val jsonBody: Option[JsValue] = request.body.asJson
    jsonBody.isDefined match {
      case false => None
      case true =>  Json.fromJson[WhaleSightingNew](jsonBody.get).asOpt
    }
  }

  // TODO Sighting FEED

}
