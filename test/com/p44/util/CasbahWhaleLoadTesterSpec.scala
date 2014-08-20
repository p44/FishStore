package com.p44.util

import com.mongodb.WriteConcern
import com.p44.db.whales.{WhalesDb, CasbahDb}
import com.p44.models.{WhaleSighting, WhaleSightingNew}
import org.specs2.mutable.Specification
import play.api.libs.iteratee.Iteratee
import play.api.libs.json.{Json, JsValue}
import reactivemongo.api.{Cursor}
import reactivemongo.bson.BSONDocument

import scala.concurrent.{Future, Await}

/**
 * Created by markwilson on 8/19/14.
 */
object CasbahWhaleLoadTesterSpec extends Specification {

  //sbt > test-only com.p44.util.CasbahWhaleLoadTesterSpec

  import com.p44.db.test.TestDbConfigCasbah

  val collection = TestDbConfigCasbah.db(WhaleSighting.collectionName)

  sequential

  val breed: String = "Blue Whale"
  val count: Int = 2
  val description: String =  "Mommy whale and baby whale."
  val timestamp: Long = System.currentTimeMillis
  val comments: List[String] = List("Excellent!")
  val objWithStubbedId = WhaleSighting("101", breed, count, description, timestamp, comments)

  "CasbahWhaleLoadTester" should {

    "insertOneWhaleSighting" in {
      // test here with WC SAFE to confirm the insert.  Run load tests with default WC Normal for a more
      // fair comparison with ReactiveMongo
      CasbahWhaleLoadTester.insertOneWhaleSighting(collection, objWithStubbedId, WriteConcern.SAFE) mustEqual true
    }
  }

  step {
    collection.dropCollection
  }
}
