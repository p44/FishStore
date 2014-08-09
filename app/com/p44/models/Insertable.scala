package com.p44.models

import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.api.{DefaultDB, QueryOpts}
import reactivemongo.bson.{BSONDocument, BSONDocumentWriter}
import reactivemongo.core.commands.{LastError, GetLastError}

import scala.concurrent.{ExecutionContext, Future}

/**
 * For models that need only perform an insert.
 * Created by markwilson on 8/4/14.
 */
trait Insertable[T] {

  val collectionName: String // name of the mongodb collection
  val lastErrorDefault: GetLastError = GetLastError() //
  val queryOptsDefault: QueryOpts = QueryOpts() //

  val persistanceWriter: BSONDocumentWriter[T]
  def toBson(obj: T): BSONDocument

  implicit val ec = ExecutionContext.Implicits.global
  implicit val writer = persistanceWriter

  def getCollection(db: DefaultDB): BSONCollection = db.collection(collectionName)

  /**
   * Inserts one creating a new mongoId in the process
   * @param db
   * @param obj
   * @return a future of the LastError which holds the result data
   */
  def insertOneAsFuture(db: DefaultDB, obj: T): Future[LastError] = {
    getCollection(db).insert(obj)
  }

}
