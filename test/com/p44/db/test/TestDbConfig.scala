package com.p44.db.test

import reactivemongo.api.MongoDriver

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}


/**
 * Created by markwilson on 8/5/14.
 */
object TestDbConfig {

  implicit val ec = ExecutionContext.Implicits.global

  val timeout: FiniteDuration = 10 seconds

  lazy val driver = new MongoDriver
  lazy val connection = driver.connection(List("localhost:27017"))
  lazy val db = {
    val dbName = "specs2-fishstore"
    val _db = connection(dbName)
    Await.ready(_db.drop, timeout)
    println("dropped test database " + dbName)
    _db
  }
}
