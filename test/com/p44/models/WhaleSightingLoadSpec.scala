package com.p44.models

import org.specs2.mutable.Specification
import play.api.libs.iteratee.Iteratee
import play.api.libs.json.{Json, JsValue}
import reactivemongo.api.{Cursor}
import reactivemongo.bson.BSONDocument

import scala.concurrent.{Future, Await}

/**
 * Created by markwilson on 8/17/14.
 */
class WhaleSightingLoadSpec  extends Specification {

  //sbt > test-only com.p44.models.WhaleSightingLoadSpec

  import com.p44.db.test.TestDbConfig._  // the test db and related data

  sequential

  implicit val reader = WhaleSighting.persistanceReader
  lazy val collection = WhaleSighting.getCollection(db)

  "WhaleSightingLoad" should {

    "clear collection" in {
      Await.result(collection.remove(BSONDocument(), WhaleSighting.lastErrorDefault, false), timeout).ok mustEqual true
    }

    "insert 1000" in {
      val generated: List[WhaleSighting] = WhaleSightingGenerator.generate(1000)
      generated.size mustEqual 1000
      val start = System.currentTimeMillis
      val f = Future { generated.foreach(x => WhaleSighting.insertOneAsFuture(collection, x)) }
      Await.result(f, timeout) // block until done
      val cmd = reactivemongo.core.commands.Count(collection.name)
      val c: Int = Await.result(db.command(reactivemongo.core.commands.Count(collection.name)), timeout)
      val elapsed = System.currentTimeMillis - start
      println("insert 1000 elapsed " + elapsed)
      c mustEqual 1000
    }

    "drop collection" in {
      Await.result(collection.drop(), timeout) mustEqual true
    }
  }

}
