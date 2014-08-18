package controllers

import com.p44.db.store.two.MongoDbStoreTwo
import com.p44.models._
import play.api.Logger
import play.api.libs.EventSource
import play.api.libs.iteratee.{Enumeratee, Concurrent}
import play.api.libs.json.{JsArray, JsValue, Json}
import play.api.mvc.{AnyContent, Request, Action, Controller}
import reactivemongo.api.{QueryOpts, DefaultDB}
import reactivemongo.core.commands.LastError

import scala.concurrent.{Await, Future, ExecutionContext}

/**
 * RESTful services for Whale Sighting social page
 * Created by markwilson on 8/9/14.
 */
object WhaleSightingController extends Controller {

  val db: DefaultDB = MongoDbStoreTwo.STORE_TWO_DB
  val timeout = MongoDbStoreTwo.timeoutDefault

  implicit val ec = ExecutionContext.global

  /** route to home page */
  def viewWhaleSightings = Action.async { request =>
    Future { Ok(views.html.whalesightings.render) }
  }

  /** POST */
  def postWhaleSighting = Action.async { request =>
    val fSighting = Future[Option[WhaleSighting]] { resolvePostWhaleSightingJsonToObj(request) }
    fSighting.flatMap { ows =>
      ows match {
        case None => Future.successful(BadRequest("Please check your request for content type of json as well as the json format."))
        case Some(ws) => {
          val fResult = WhaleSighting.insertOneAsFuture(db, ws)
          fResult.map { le =>
            le.ok match {
              case true => {
                lookupAndPushToChannel(ws) // hits the db and pushes to stream out with the new id
                Ok("Saved")
              }
              case _ => InternalServerError("Failed to save.")
            }
          }
        }
      }
    }
  }

  /**
   * POST for load test
   * The Action is synchronous so that the db call is multi-threaded but the action logic is synchronous blocking.
   * This is useful to see how the cores are used at the db layer under load
   */
  def postWhaleSightingBlockingAction = Action { request =>
    val ows: Option[WhaleSighting] = resolvePostWhaleSightingJsonToObj(request)
    ows match {
      case None => BadRequest("Please check your request for content type of json as well as the json format.")
      case Some(ws) => {
        val le = Await.result(WhaleSighting.insertOneAsFuture(db, ws), timeout) // Block this thread
        le.ok match {
          case true => {
            lookupAndPushToChannel(ws) // hits the db and pushes to stream out with the new id
            Ok("Saved")
          }
          case _ => InternalServerError("Failed to save.")
        }
      }
    }
  }


  /**
   * GET
   *
   * Generates whale sightings to the size specified and inserts
   * to a test specific database
   *
   * @param size
   * @return
   */
  def loadTest(size: Int) = Action.async { request =>
    val generated: List[WhaleSighting] = WhaleSightingGenerator.generate(1000)
    println("Load Test, generated " + generated.size)
    println("Load Test, inserting... ")
    val start = System.currentTimeMillis
    val f = Future {
      val collLoadTest = WhaleSighting.getCollection(MongoDbStoreTwo.WHALE_SIGHTING_LOAD_DB)
      generated.foreach(x => WhaleSighting.insertOneAsFuture(collLoadTest, x))
    }
    f.map { x =>
      println("Load Test, inserting done.")
      Ok("Load Test, inserted " + size)
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
   * GET with limit
   *
   * sorts by timestamp most recent
   *
   * @param limit
   * @return
   */
  def getWhaleSightingMany(limit: Int) = Action.async { request =>
    val q = WhaleSighting.findMongoIdExists
    val sort = WhaleSighting.sortTimestampDesc
    val opts = Some(new QueryOpts(0, 10, 0)) //(skipN: Int = 0, batchSizeN: Int = 0, flagsN: Int = 0)
    val fSightings: Future[List[WhaleSighting]] = WhaleSighting.findMultipleByQueryWithSortAsFuture(db, q, sort, opts)
    fSightings.map { sightings =>
      Ok(Json.prettyPrint(WhaleSighting.toJsArray(sightings)))
    }
  }

  /**
   * PUT - add a comment
   *
   * @param id Mongo _id hex string
   */
  def putWhaleSightingAddComment(id: String) = Action.async { request =>
    val fComment = Future[Option[Comment]] { resolveCommentJsonToObj(request) }
    fComment.flatMap { oc =>
      oc match {
        case None => Future.successful(BadRequest("Please check your request for content type of json as well as the json format."))
        case Some(c) => {
          val q = WhaleSighting.findById(id)
          val modifier = WhaleSighting.modifierAddComment(c.comment)
          val fLastError: Future[LastError] = WhaleSighting.updateOneAsFuture(db, q, modifier)
          fLastError.map { le =>
            le.ok match {
              case false => BadRequest("None found or failed to remove for id " + id)
              case _ => Ok("Added Comment for id " + id)
            }
          }
        }
      }
    }
  }

  /**
   * REMOVE
   *
   * @param id
   */
  def removeWhaleSighting(id: String) = Action.async { request =>
    val q = WhaleSighting.findById(id)
    val firstMatchOnly = false // remove all with this id, though should only be one
    val fLastError: Future[LastError] = WhaleSighting.getCollection(db).remove(q, WhaleSighting.lastErrorDefault, firstMatchOnly)
    fLastError.map { le =>
      le.ok match {
        case false => BadRequest("None found or failed to remove for id " + id)
        case _ => Ok("Removed " + id)
      }
    }
  }

  case class PostWhaleSighting(breed: String, count: Int, description: String)
  object PostWhaleSighting {
    implicit val jsonWriter = Json.writes[PostWhaleSighting]
    implicit val jsonReader = Json.reads[PostWhaleSighting]
  }

  /** for POST Json to WhaleSightingNew
    * on successful json parse to WhaleSighting calls pushToWhaleSightingChannel
    */
  def resolvePostWhaleSightingJsonToObj(request: Request[AnyContent]): Option[WhaleSighting] = {
    val jsonBody: Option[JsValue] = request.body.asJson
    jsonBody.isDefined match {
      case false => None
      case true =>  {
        val jsv = jsonBody.get
        val opws: Option[PostWhaleSighting] = jsv.validate[PostWhaleSighting].asOpt
        opws match {
          case None => None
          case Some(pws) => {
            // _id: String, breed: String, count: Int, description: String, timestamp: Long, comments: List[String]
            val now = System.currentTimeMillis
            val wsn = WhaleSighting(WhaleSighting.ID_NONE, pws.breed, pws.count, pws.description, now, List(""))
            Some(wsn)
          }
        }
      }
    }
  }

  /**
   * Lookup by non-id query and publish
   * @param wsNew a new whale sighting with no id
   */
  def lookupAndPushToChannel(wsNew: WhaleSighting): Unit = {
    Future {
      val q = WhaleSighting.findByBody(wsNew)
      val fFound: Future[Option[WhaleSighting]] = WhaleSighting.findOneByQueryAsFuture(WhaleSighting.getCollection(db), q)
      fFound.map { ows =>
        ows match {
          case None => println("lookupAndPushToChannel ERROR lookup failed for " + wsNew)
          case Some(ws) => pushToWhaleSightingChannel(ws)
        }
      }
    }
  }

  /** for PUT add comment */
  def resolveCommentJsonToObj(request: Request[AnyContent]): Option[Comment] = {
    val jsonBody: Option[JsValue] = request.body.asJson
    jsonBody.isDefined match {
      case false => None
      case true =>  Json.fromJson[Comment](jsonBody.get).asOpt
    }
  }


  // Streaming out whale sightings to anybody interested...

  /**
   * Hub for distributing Whale Sighting messages
   *  fishStoreTwoOut: Enumerator[JsValue]
   *  fishStoreTwoChannel: Channel[JsValue]
   */
  val (whaleSightingOut, whaleSightingChannel) = Concurrent.broadcast[JsValue]

  def pushToWhaleSightingChannel(obj: WhaleSighting): Unit = {  pushToWhaleSightingChannelAsJsv(Json.toJson(obj)) }
  def pushToWhaleSightingChannelAsJsv(jsv: JsValue): Unit = { Future { whaleSightingChannel.push(jsv) } }

  /** Enumeratee for detecting disconnect of the stream */
  def connDeathWatch(addr: String): Enumeratee[JsValue, JsValue] = {
    Enumeratee.onIterateeDone { () =>
      Logger.info(addr + " - whaleSightingOut disconnected")
    }
  }

  /**
   * Controller action serving activity for new whale sightings
   */
  def whaleSightingsFeed = Action { req =>
    Logger.info("FEED whaleSightingsFeed - " + req.remoteAddress + " - WhaleSightings Out connected")
    Ok.chunked(whaleSightingOut
      &> Concurrent.buffer(100)
      &> connDeathWatch(req.remoteAddress)
      &> EventSource()).as("text/event-stream") // &>  Compose this Enumerator with an Enumeratee. Alias for 'through'
  }

}
