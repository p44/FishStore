package com.p44.db

import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.api.{DefaultDB, FailoverStrategy, QueryOpts}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}
import reactivemongo.core.commands.{GetLastError, LastError}

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by markwilson on 8/3/14.
 */
trait Persistable[T] {

  val collectionName: String // name of the mongodb collection
  val failoverStrategy: Option[FailoverStrategy] = None // optional
  val lastErrorDefault: GetLastError = GetLastError(false, None, 0, true)
// GetLastError() is GetLastError(false, None, 0, false)
//@param j Make sure that the previous operation has been committed into the journal. Journaling must be enabled on the servers.
//@param w Specify the level of replication for a write operation. Should be a BSONString or BSONInteger. See [[http://docs.mongodb.org/manual/reference/command/getLastError/#dbcmd.getLastError the MongoDB documentation]].
//@param wtimeout Write propagation timeout (in milliseconds). See [[http://docs.mongodb.org/manual/reference/command/getLastError/#dbcmd.getLastError the MongoDB documentation]].
//@param fsync Make sure that the previous (write) operation has been written on the disk.
  val queryOptsDefault: QueryOpts = QueryOpts() // QueryOpts(skipN: Int = 0, batchSizeN: Int = 0, flagsN: Int = 0)

  val persistanceWriter: BSONDocumentWriter[T]
  val persistanceReader: BSONDocumentReader[T]
  def toBson(obj: T): BSONDocument
  def fromBson(doc: BSONDocument): T


  implicit val ec = ExecutionContext.Implicits.global
  implicit val writer = persistanceWriter
  implicit val reader = persistanceReader

  def getCollection(db: DefaultDB): BSONCollection = {
    failoverStrategy.isDefined match {
      case false => db.collection(collectionName)
      case _ => db.collection(collectionName, failoverStrategy.get)
    }
  }

  // MongoId converters
  def getMongoId(doc: BSONDocument, fieldName_id: String): String = {
    val oid: Option[BSONObjectID] = doc.getAs[BSONObjectID](fieldName_id)
    oid match {
      case None => throw new Exception("Failed mongoId conversion for field name " + fieldName_id)
      case Some(id) => id.stringify // hexadecimal String representation
    }
  }
  def hexStringToMongoId(hexString: String): BSONObjectID = BSONObjectID(hexString)

  /** special inert document with no identifier if using mongoId as the primary id - Allows a stubbed out unused _id */
  def getBsonForInsert(obj: T): BSONDocument

  def insertOneAsFuture(db: DefaultDB, obj: T): Future[LastError] = {
    insertOneAsFuture(getCollection(db), obj)
  }

  /** calls collection.insert(getBsonForInsert(obj))
    * getBsonForInsert will ignore the present id value letting mongodb gen the _id */
  def insertOneAsFuture(collection: BSONCollection, obj: T): Future[LastError] = {
    collection.insert(getBsonForInsert(obj))
  }

  def updateOneAsFuture(db: DefaultDB, selector: BSONDocument, modifier: BSONDocument, getLastError: GetLastError = lastErrorDefault): Future[LastError] = {
    updateOneAsFuture(getCollection(db), selector, modifier, getLastError)
  }

  def updateOneAsFuture(collection: BSONCollection, selector: BSONDocument, modifier: BSONDocument, getLastError: GetLastError): Future[LastError] = {
    collection.update(selector, modifier, getLastError)
  }

  def findOneByQueryAsFuture(db: DefaultDB, query: BSONDocument): Future[Option[T]] = {
    findOneByQueryAsFuture(getCollection(db), query)
  }

  def findOneByQueryAsFuture(collection: BSONCollection, query: BSONDocument): Future[Option[T]] = {
    collection.find(query).one[T]
  }

  def findMultipleByQueryWithSortAsFuture(db: DefaultDB, query: BSONDocument, sort: BSONDocument, opts: Option[QueryOpts]): Future[List[T]] = {
    findMultipleByQueryWithSortAsFuture(getCollection(db), query, sort, opts)
  }

  def findMultipleByQueryWithSortAsFuture(collection: BSONCollection, query: BSONDocument, sort: BSONDocument, opts: Option[QueryOpts]): Future[List[T]] = {
    opts.isDefined match {
      case true => {
        val gqb = collection.find(query).sort(sort).options(opts.get) // GenericQueryBuilder
        val c = gqb.cursor[T]
        c.collect[List]()
      }
      case _ => collection.find(query).sort(sort).cursor[T].collect[List]()
    }
  }

  def findOneByQueryWithProjectionAsFuture(collection: BSONCollection, query: BSONDocument, projection: BSONDocument): Future[Option[BSONDocument]] = {
    collection.find(query, projection).one[BSONDocument]
  }

  /**
   * If the param objAsDoc has an _id then an update will be called, otherwise an insert is called
   * via mongo save()
   *
   * @param collection
   * @param objAsDoc
   * @param getLastError
   * @return
   */
  def saveOneAsFuture(collection: BSONCollection, objAsDoc: BSONDocument, getLastError: GetLastError = lastErrorDefault): Future[LastError] = {
    collection.save(objAsDoc, getLastError)
  }
}
