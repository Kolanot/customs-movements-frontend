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

package crypto

import forms.ConsignmentReferences
import javax.inject._
import models.cache.{Answers, ArrivalAnswers, Cache}
import uk.gov.hmrc.crypto.{CompositeSymmetricCrypto, Crypted, PlainText}

@Singleton
class Crypto @Inject()(crypto: CompositeSymmetricCrypto) {

  def encrypt(cache: Cache): Cache =
    applyCrypto(cache)(encryptString)

  def decrypt(cache: Cache): Cache =
    applyCrypto(cache)(decryptString)

  def encryptString: String => String = { str: String =>
    crypto.encrypt(PlainText(str)).value
  }

  private def decryptString: String => String = { str: String =>
    crypto.decrypt(Crypted(str)).value
  }

  private def applyCrypto(cache: Cache)(f: String => String): Cache =
    cache.copy(eori = f(cache.eori), answers =  applyCrypto(cache.answers)(f))

  private def applyCrypto(answers: Answers)(f: String => String): Answers =
    answers match {
      case arrivalAnswers: ArrivalAnswers => applyCrypto(arrivalAnswers)(f)
        // TODO - other answer types
      case other: Answers => other
    }

  private def applyCrypto(answers: ArrivalAnswers)(f: String => String): ArrivalAnswers =
    answers.copy(
      consignmentReferences = answers.consignmentReferences.map(refs => applyCrypto(refs)(f)),
      arrivalDetails = answers.arrivalDetails,
      location = answers.location
    )

  private def applyCrypto(refs: ConsignmentReferences)(f: String => String): ConsignmentReferences =
    refs.copy(reference = refs.reference, referenceValue = f(refs.referenceValue))

}
