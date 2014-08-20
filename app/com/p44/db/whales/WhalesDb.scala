package com.p44.db.whales


import com.p44.db.store.two.MongoDbStoreTwo
import com.p44.models.FishStoreModels
import reactivemongo.api.{MongoDriver, DefaultDB, MongoConnection}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration.FiniteDuration

/**
 * Created by markwilson on 8/19/14.
 */
object WhalesDb {

  val driver: MongoDriver = MongoDbStoreTwo.driver // reuse the fish store driver
  val hosts: List[String] = FishStoreModels.FISHSTORE_TWO_DB_HOSTS

  val WHALE_SIGHTING_LOAD_DB_NAME = "whale-sighting-load" // special separate db for load testing
  lazy val connWhaleSightingLoadDb: MongoConnection = driver.connection(hosts)
  lazy val WHALE_SIGHTING_LOAD_DB: DefaultDB = connWhaleSightingLoadDb.db(WHALE_SIGHTING_LOAD_DB_NAME)

}
