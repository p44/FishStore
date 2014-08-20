package com.p44.db.test

import com.mongodb.MongoURI
import com.mongodb.casbah.Imports._
import com.p44.models.FishStoreModels

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
 * Created by markwilson on 8/19/14.
 */
object TestDbConfigCasbah {

  implicit val ec = ExecutionContext.Implicits.global

  val timeout: FiniteDuration = 10 seconds

  val TEST_CONNECTION = MongoConnection(new MongoURI("mongodb://127.0.0.1:27017/"))
  val db = TEST_CONNECTION("specs2-fishstore")

}
