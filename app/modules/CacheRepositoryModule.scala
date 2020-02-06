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

package modules

import crypto.LocalCrypto
import play.api.inject.Binding
import play.api.{Configuration, Environment, Logger}
import repositories.{CacheMongoRepository, CacheRepository, EncryptedCacheMongoRepository}
import uk.gov.hmrc.crypto.CompositeSymmetricCrypto

class CacheRepositoryModule extends play.api.inject.Module {

  private val logger = Logger(this.getClass)

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {

    val repositoryBinding: Binding[CacheRepository] =
      if (configuration.getOptional[Boolean]("mongodb.encryption.enabled").getOrElse(false)) {
        logger.info("Mongo encryption ENABLED")
        bind[CacheRepository].to[EncryptedCacheMongoRepository]
      } else {
        logger.info("Mongo encryption disabled")
        bind[CacheRepository].to[CacheMongoRepository]
      }
    Seq(bind[CompositeSymmetricCrypto].to(classOf[LocalCrypto]), repositoryBinding)
  }
}
