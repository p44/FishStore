package com.p44.util

import com.mongodb.{WriteConcern, DBObject}
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}
import com.p44.db.whales.{WhalesDb, CasbahDb}
import com.p44.models.WhaleSighting

/**
 * Created by markwilson on 8/19/14.
 *
 * Specifically for inserting to the load test db using casbah instead of Reactive Mongo
 */
object CasbahWhaleLoadTester {

  lazy val collectionWhaleSightingInLoadDb: MongoCollection = {
    CasbahDb.getCollection(WhalesDb.WHALE_SIGHTING_LOAD_DB_NAME, WhaleSighting.collectionName)
  }

  /**
   * Insert One (blocking), defaults to fastest insert at WriteConcern.NORMAL
   *
   * @param ws
   * @return if error is empty (true if no error)
   */
  def insertOneWhaleSighting(coll: MongoCollection, ws: WhaleSighting, wc: WriteConcern = WriteConcern.NORMAL): Boolean = {
    val wr = coll.insert(convertWhaleSightingToMongoObject(ws), wc)
    if (wc == WriteConcern.SAFE) {
      if (wr == null || wr.getCachedLastError == null) false
      else wr.getCachedLastError.ok
    }
    else true // assumes write concern normal, no last error to check

  }

  def convertWhaleSightingToMongoObject(ws: WhaleSighting): DBObject = {
    val builder = MongoDBObject.newBuilder
    builder += WhaleSighting.fieldBreed -> ws.breed
    builder += WhaleSighting.fieldCount -> ws.count
    builder += WhaleSighting.fieldDescription -> ws.description
    builder += WhaleSighting.fieldTimestamp -> ws.timestamp
    builder += WhaleSighting.fieldComments -> MongoDBList(ws.comments: _*)
    builder.result()
  }

}
