/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package repositories

import crypto.Crypto
import javax.inject.{Inject, Singleton}
import models.cache.Cache
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats.objectIdFormats

import scala.concurrent.{ExecutionContext, Future}

trait CacheRepository {

  def removeByEori(eori: String): Future[Unit]

  def findOrCreate(eori: String, onMissing: Cache): Future[Cache]

  def findByEori(eori: String): Future[Option[Cache]]

  def upsert(cache: Cache): Future[Cache]
}

@Singleton
class EncryptedCacheMongoRepository @Inject()(repository: CacheMongoRepository, crypto: Crypto) extends CacheRepository {

  import scala.concurrent.ExecutionContext.Implicits.global

  private def encrypt: Cache => Cache = crypto.encrypt

  private def decrypt: Cache => Cache = crypto.decrypt

  private def encryptString: String => String = crypto.encryptString

  override def removeByEori(eori: String): Future[Unit] = repository.removeByEori(encryptString(eori))

  override def findOrCreate(eori: String, onMissing: Cache): Future[Cache] =
    repository.findOrCreate(crypto.encryptString(eori), encrypt(onMissing)).map(decrypt)

  override def findByEori(eori: String): Future[Option[Cache]] = repository.findByEori(crypto.encryptString(eori)).map(_.map(decrypt))

  override def upsert(cache: Cache): Future[Cache] = repository.upsert(encrypt(cache)).map(decrypt)
}

@Singleton
class CacheMongoRepository @Inject()(mc: ReactiveMongoComponent)(implicit ec: ExecutionContext)
    extends ReactiveRepository[Cache, BSONObjectID]("cache", mc.mongoConnector.db, Cache.format, objectIdFormats) with CacheRepository {

  override def indexes: Seq[Index] = Seq(Index(Seq("eori" -> IndexType.Ascending), name = Some("eoriIdx")))

  override def removeByEori(eori: String): Future[Unit] = remove("eori" -> eori).filter(_.ok).map(_ => (): Unit)

  override def findOrCreate(eori: String, onMissing: Cache): Future[Cache] =
    findByEori(eori).flatMap {
      case Some(movementCache) => Future.successful(movementCache)
      case None                => save(onMissing)
    }

  override def findByEori(eori: String): Future[Option[Cache]] = find("eori" -> eori).map(_.headOption)

  private def save(movementCache: Cache): Future[Cache] = insert(movementCache).map { res =>
    if (!res.ok) logger.error(s"Errors when persisting movement cache: ${res.writeErrors.mkString("--")}")
    movementCache
  }

  override def upsert(cache: Cache): Future[Cache] =
    findAndUpdate(Json.obj("eori" -> cache.eori), Json.toJson(cache).as[JsObject])
      .map(_.value.map(_.as[Cache]))
      .flatMap {
        case Some(cache) => Future.successful(cache)
        case None        => save(cache)
      }
}
