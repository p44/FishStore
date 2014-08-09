package com.p44.models

import play.api.libs.json.{JsArray, Json}
import reactivemongo.bson.{BSONDocument, BSONDocumentWriter}

/**
 * A reported new whale sighting, an id is not yet determined by the data store.
 * See WhaleSighting for an identified case class
 */
case class WhaleSightingNew(breed: String, count: Int, description: String, timestamp: Long, comments: List[String])

/**
 * Companion Object for inserting a new WhaleSighting
 */
object WhaleSightingNew extends Insertable[WhaleSightingNew] {
  val collectionName = WhaleSighting.collectionName

  val fieldBreed = WhaleSighting.fieldBreed
  val fieldCount = WhaleSighting.fieldCount
  val fieldDescription = WhaleSighting.fieldDescription
  val fieldTimestamp = WhaleSighting.fieldTimestamp
  val fieldComments = WhaleSighting.fieldComments

  lazy val empty = WhaleSightingNew("", 0, "", 0L, Nil)

  implicit object persistanceWriter extends BSONDocumentWriter[WhaleSightingNew] {
    def write(obj: WhaleSightingNew): BSONDocument = toBson(obj: WhaleSightingNew)
  }

  def toBson(wsn: WhaleSightingNew): BSONDocument = {
    BSONDocument(
      fieldBreed -> wsn.breed,
      fieldCount -> wsn.count,
      fieldDescription -> wsn.description,
      fieldTimestamp -> wsn.timestamp,
      fieldComments -> wsn.comments)
  }

  // JSON
  implicit val jsonWriter = Json.writes[WhaleSightingNew] // Json.toJson(obj): JsValue
  implicit val jsonReader = Json.reads[WhaleSightingNew] // Json.fromJson[T](jsval): JsResult[T] .asOpt Option[T]
  def toJsArray(objs: List[WhaleSightingNew]): JsArray = JsArray(objs.map(Json.toJson(_)).toSeq)

}
