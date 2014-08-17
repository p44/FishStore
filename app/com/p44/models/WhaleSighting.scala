package com.p44.models

import play.api.libs.json.{JsArray, Json}
import reactivemongo.bson.{BSONObjectID, BSONDocument, BSONDocumentReader, BSONDocumentWriter}

/**
 * A reported whale sighting with identifier
 */
case class WhaleSighting(_id: String, breed: String, count: Int, description: String, timestamp: Long, comments: List[String])

/**
 * Companion object
 */
object WhaleSighting extends Persistable[WhaleSighting]{

  val collectionName = "whale_sighting"

  val fieldMongoId = "_id"
  val fieldBreed = "breed"
  val fieldCount = "count"
  val fieldDescription = "descrioption"
  val fieldTimestamp = "timestamp"
  val fieldComments = "comments"

  lazy val empty = WhaleSighting("0", "", 0, "", 0L, Nil)
  val ID_NONE = "0"

  implicit object persistanceWriter extends BSONDocumentWriter[WhaleSighting] {
    def write(obj: WhaleSighting): BSONDocument = toBson(obj: WhaleSighting)
  }

  implicit object persistanceReader extends BSONDocumentReader[WhaleSighting] {
    def read(doc: BSONDocument): WhaleSighting = fromBson(doc)
  }

  def toBson(ws: WhaleSighting): BSONDocument = {
    BSONDocument(
      fieldMongoId -> ws._id,
      fieldBreed -> ws.breed,
      fieldCount -> ws.count,
      fieldDescription -> ws.description,
      fieldTimestamp -> ws.timestamp,
      fieldComments -> ws.comments)
  }

  def fromBson(doc: BSONDocument): WhaleSighting = {
    WhaleSighting(
      getMongoId(doc, fieldMongoId),
      doc.getAs[String](fieldBreed).get,
      doc.getAs[Int](fieldCount).get,
      doc.getAs[String](fieldDescription).get,
      doc.getAs[Long](fieldTimestamp).get,
      doc.getAs[List[String]](fieldComments).toList.flatten)
  }

  /**
   * Returns bson with no _id for inserting as the _id is the mongoId which mongo will create on insert
   *
   * @param ws
   * @return
   */
  def getBsonForInsert(ws: WhaleSighting): BSONDocument = {
    BSONDocument(
      fieldBreed -> ws.breed,
      fieldCount -> ws.count,
      fieldDescription -> ws.description,
      fieldTimestamp -> ws.timestamp,
      fieldComments -> ws.comments)
  }

  // selectors
  def findTimestampExists: BSONDocument = { BSONDocument(fieldTimestamp -> BSONDocument("$exists" -> true)) }
  def findMongoIdExists: BSONDocument = { BSONDocument(fieldMongoId -> BSONDocument("$exists" -> true)) }
  def findById(midAsHexString: String): BSONDocument = { BSONDocument(fieldMongoId -> hexStringToMongoId(midAsHexString)) }
  def findByTimestamp(ts: Long): BSONDocument = { BSONDocument(fieldTimestamp -> ts) }
  def findByBody(ws: WhaleSighting): BSONDocument = {
    BSONDocument(fieldTimestamp -> ws.timestamp, fieldCount -> ws.count, fieldBreed -> ws.breed, fieldDescription -> ws.description)
  }

  // sorts
  def sortTimestampDesc: BSONDocument = BSONDocument(fieldTimestamp -> -1)
  def sortTimestampAsc: BSONDocument = BSONDocument(fieldTimestamp -> 1)

  // modifiers
  def modifierSetDescription(s: String): BSONDocument = { BSONDocument("$set" -> BSONDocument(fieldDescription -> s)) }
  def modifierAddComment(s: String): BSONDocument = { BSONDocument("$addToSet" -> BSONDocument(fieldComments -> s)) }

  // JSON
  implicit val jsonWriter = Json.writes[WhaleSighting] // Json.toJson(obj): JsValue
  implicit val jsonReader = Json.reads[WhaleSighting] // Json.fromJson[T](jsval): JsResult[T] .asOpt Option[T]
  def toJsArray(objs: List[WhaleSighting]): JsArray = JsArray(objs.map(Json.toJson(_)).toSeq)
}


/**
 * A generator for load testing.
 */
object WhaleSightingGenerator {
  val breeds = List("Right Whale", "Bowhead Whale", "Gray Whale", "Fin Whale", "Sei Whale", "Blue Whale", "Narwhal", "Beluga")
  def getRandomCount: Int = scala.util.Random.nextInt(5)
  def getRandomBreed: String = breeds(scala.util.Random.nextInt(breeds.size))

  /**
   * Generate this many Whale Sightings
   *
   * @param size
   * @return
   */
  def generate(size: Int): List[WhaleSighting] = {
    if (size < 1) List()
    else {
      val r = for (i <- 0 until size) yield {
        WhaleSighting(WhaleSighting.ID_NONE, getRandomBreed, getRandomCount, "This is auto-generated.", System.currentTimeMillis, List())
      }
      r.toList
    }
  }
}
