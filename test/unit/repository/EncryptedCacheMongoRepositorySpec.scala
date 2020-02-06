package repository
import crypto.Crypto
import models.cache.Cache
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers._
import repositories.{CacheMongoRepository, EncryptedCacheMongoRepository}
import unit.base.UnitSpec

import scala.concurrent.Future.successful

class EncryptedCacheMongoRepositorySpec extends UnitSpec with BeforeAndAfterEach {

  private val rawCache = mock[Cache]
  private val rawCacheSaved = mock[Cache]
  private val encryptedCache = mock[Cache]
  private val encryptedCacheSaved = mock[Cache]
  private val rawEori = "eori"
  private val encryptedEori = "encryptedEori"

  private val crypto = mock[Crypto]
  private val underlyingRepo = mock[CacheMongoRepository]
  private val repo = new EncryptedCacheMongoRepository(underlyingRepo, crypto)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    given(crypto.encrypt(rawCache)) willReturn encryptedCache
    given(crypto.decrypt(encryptedCacheSaved)) willReturn rawCacheSaved
    given(crypto.encryptString).willReturn((_: String) => encryptedEori)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(underlyingRepo)
  }

  "removeByEori" should {
    "Encrypt and delegate to Repository" in {
      given(underlyingRepo.removeByEori(encryptedEori)) willReturn successful((): Unit)
      await(repo.removeByEori(rawEori))
      verify(underlyingRepo).removeByEori(encryptedEori)
    }
  }

  "findOrCreate" should {
    "Encrypt and delegate to Repository" in {
      given(underlyingRepo.findOrCreate(encryptedEori, encryptedCache)) willReturn successful(encryptedCacheSaved)
      await(repo.findOrCreate(rawEori, rawCache)) mustBe rawCacheSaved
      verify(underlyingRepo).findOrCreate(encryptedEori, encryptedCache)
    }
  }

  "findByEori" should {
    "Encrypt and delegate to Repository" in {
      given(underlyingRepo.findByEori(encryptedEori)) willReturn successful(Some(encryptedCacheSaved))
      await(repo.findByEori(rawEori)) mustBe Some(rawCacheSaved)
      verify(underlyingRepo).findByEori(encryptedEori)
    }
  }

  "upsert" should {
    "Encrypt and delegate to Repository" in {
      given(underlyingRepo.upsert(encryptedCache)) willReturn successful(encryptedCacheSaved)
      await(repo.upsert(rawCache)) mustBe rawCacheSaved
      verify(underlyingRepo).upsert(encryptedCache)
    }
  }
}
