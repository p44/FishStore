package com.p44.db.whales

import com.mongodb.MongoURI
import com.mongodb.casbah.Imports._

/**
 * Created by markwilson on 8/19/14.
 *
 * This is for comparison load testing.
 * Originally intended to hit the separate load testing databases for load test comparisons
 */
object CasbahDb {

  val HOSTS_CONNECTION = MongoConnection(new MongoURI("mongodb://127.0.0.1:27017/")) // connection pool to the host(s)

  /**
   * Usage:  getCollection()  */
  def getCollection(dbName: String, collection: String): MongoCollection = {
    HOSTS_CONNECTION(dbName)(collection)
  }

}
