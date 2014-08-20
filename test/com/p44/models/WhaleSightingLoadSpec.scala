package com.p44.models

import org.specs2.mutable.Specification
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
      val elapsed = System.currentTimeMillis - start
      println("insert 1000 elapsed " + elapsed)
      val q = WhaleSighting.findMongoIdExists
      val sort = WhaleSighting.sortTimestampDesc
      val opts = None
      val r: List[WhaleSighting] = Await.result(WhaleSighting.findMultipleByQueryWithSortAsFuture(db, q, sort, opts), timeout)
      println("find multiple r " + r.size)
      r.size mustEqual 1000  // FAIL (sometimes) '997' is not equal to '1000' (WhaleSightingLoadSpec.scala:41)
      val cmd = reactivemongo.core.commands.Count(collection.name)
      val c: Int = Await.result(db.command(reactivemongo.core.commands.Count(collection.name)), timeout)
      c mustEqual 1000 // TODO: FAIL (sometimes) '997' is not equal to '1000'
    }

    "drop collection" in {
      Await.result(collection.drop(), timeout) mustEqual true
    }

    "getRandomCount" in {
      val firstLessThan: (Int, Int) => Boolean = (a: Int, b:Int ) => { a < b }

      firstLessThan(0, WhaleSightingGenerator.getRandomCount) mustEqual true // cant equal 0
      firstLessThan(0, WhaleSightingGenerator.getRandomCount) mustEqual true // cant equal 0
      firstLessThan(0, WhaleSightingGenerator.getRandomCount) mustEqual true // cant equal 0
      firstLessThan(0, WhaleSightingGenerator.getRandomCount) mustEqual true // cant equal 0
      firstLessThan(0, WhaleSightingGenerator.getRandomCount) mustEqual true // cant equal 0
      firstLessThan(0, WhaleSightingGenerator.getRandomCount) mustEqual true // cant equal 0
      firstLessThan(0, WhaleSightingGenerator.getRandomCount) mustEqual true // cant equal 0
      firstLessThan(0, WhaleSightingGenerator.getRandomCount) mustEqual true // cant equal 0

      firstLessThan(WhaleSightingGenerator.getRandomCount, 6) mustEqual true // cant >= 6
      firstLessThan(WhaleSightingGenerator.getRandomCount, 6) mustEqual true // cant >= 6
      firstLessThan(WhaleSightingGenerator.getRandomCount, 6) mustEqual true // cant >= 6
      firstLessThan(WhaleSightingGenerator.getRandomCount, 6) mustEqual true // cant >= 6
      firstLessThan(WhaleSightingGenerator.getRandomCount, 6) mustEqual true // cant >= 6
      firstLessThan(WhaleSightingGenerator.getRandomCount, 6) mustEqual true // cant >= 6
      firstLessThan(WhaleSightingGenerator.getRandomCount, 6) mustEqual true // cant >= 6
      firstLessThan(WhaleSightingGenerator.getRandomCount, 6) mustEqual true // cant >= 6
      firstLessThan(WhaleSightingGenerator.getRandomCount, 6) mustEqual true // cant >= 6
    }
  }

}
