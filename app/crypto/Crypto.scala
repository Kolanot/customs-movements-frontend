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

import forms._
import javax.inject._
import models.cache._
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
    cache.copy(eori = f(cache.eori), answers = applyCrypto(cache.answers)(f))

  private def applyCrypto(answers: Answers)(f: String => String): Answers =
    answers match {
      case arrivalAnswers: ArrivalAnswers                 => applyCrypto(arrivalAnswers)(f)
      case departureAnswers: DepartureAnswers             => applyCrypto(departureAnswers)(f)
      case associateUcrAnswers: AssociateUcrAnswers       => applyCrypto(associateUcrAnswers)(f)
      case disassociateUcrAnswers: DisassociateUcrAnswers => applyCrypto(disassociateUcrAnswers)(f)
      case shutMucrAnswers: ShutMucrAnswers               => applyCrypto(shutMucrAnswers)(f)
      case other: Answers                                 => throw new RuntimeException(s"Missing applyCrypto for answer ${other.`type`}")
    }

  private def applyCrypto(answers: ArrivalAnswers)(f: String => String): ArrivalAnswers =
    answers.copy(
      consignmentReferences = answers.consignmentReferences.map(refs => applyCrypto(refs)(f)),
      arrivalDetails = answers.arrivalDetails,
      location = answers.location
    )

  private def applyCrypto(answers: DepartureAnswers)(f: String => String): DepartureAnswers =
    answers.copy(
      consignmentReferences = answers.consignmentReferences.map(refs => applyCrypto(refs)(f)),
      departureDetails = answers.departureDetails,
      location = answers.location,
      transport = answers.transport
    )

  private def applyCrypto(answers: AssociateUcrAnswers)(f: String => String): AssociateUcrAnswers =
    answers.copy(
      mucrOptions = answers.mucrOptions.map(opts => applyCrypto(opts)(f)),
      associateUcr = answers.associateUcr.map(ucr => applyCrypto(ucr)(f))
    )

  private def applyCrypto(answers: DisassociateUcrAnswers)(f: String => String): DisassociateUcrAnswers =
    answers.copy(ucr = answers.ucr.map(ucr => applyCrypto(ucr)(f)))

  private def applyCrypto(answers: ShutMucrAnswers)(f: String => String): ShutMucrAnswers =
    answers.copy(shutMucr = answers.shutMucr.map(shutMucr => applyCrypto(shutMucr)(f)))

  private def applyCrypto(shutMucr: ShutMucr)(f: String => String): ShutMucr =
    shutMucr.copy(mucr = f(shutMucr.mucr))

  private def applyCrypto(disassociateUcr: DisassociateUcr)(f: String => String): DisassociateUcr =
    disassociateUcr.copy(
      kind = disassociateUcr.kind,
      ducr = disassociateUcr.ducr.map(ducr => f(ducr)),
      mucr = disassociateUcr.mucr.map(mucr => f(mucr))
    )

  private def applyCrypto(associateUcr: AssociateUcr)(f: String => String): AssociateUcr =
    associateUcr.copy(kind = associateUcr.kind, ucr = f(associateUcr.ucr))

  private def applyCrypto(mucrOptions: MucrOptions)(f: String => String): MucrOptions =
    mucrOptions.copy(newMucr = f(mucrOptions.newMucr), existingMucr = f(mucrOptions.existingMucr), createOrAdd = mucrOptions.createOrAdd)

  private def applyCrypto(refs: ConsignmentReferences)(f: String => String): ConsignmentReferences =
    refs.copy(reference = refs.reference, referenceValue = f(refs.referenceValue))

}
