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

import java.time.{LocalDate, LocalTime}
import java.util.UUID

import forms._
import forms.common.{Date, Time}
import models.cache._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import uk.gov.hmrc.crypto.{CompositeSymmetricCrypto, Crypted, PlainText}
import unit.base.UnitSpec

class CryptoSpec extends UnitSpec {

  private val simmetricCrypto = mock[CompositeSymmetricCrypto]
  private val crypto = new Crypto(simmetricCrypto)

  "encrypt()" should {

    val k = UUID.randomUUID().toString
    when(simmetricCrypto.encrypt(any[PlainText]())).thenReturn(Crypted(k))

    "encrypt Arrival Answers" in {
      val answers = ArrivalAnswers(
        consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345")),
        arrivalDetails = Some(ArrivalDetails(Date(LocalDate.now()), Time(LocalTime.now()))),
        location = Some(Location("code"))
      )
      val cache = Cache("eori", answers)
      val enc = crypto.encrypt(cache)

      enc mustBe cache.copy(eori = k, answers = answers.copy(consignmentReferences = Some(ConsignmentReferences("M", k))))
    }

    "encrypt Departure Answers" in {
      val answers = DepartureAnswers(
        consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345")),
        departureDetails = Some(DepartureDetails(Date(LocalDate.now()), Time(LocalTime.now()))),
        location = Some(Location("code")),
        transport = Some(Transport("mode", "id", "nat"))
      )
      val cache = Cache("eori", answers)
      val enc = crypto.encrypt(cache)

      enc mustBe cache.copy(eori = k, answers = answers.copy(consignmentReferences = Some(ConsignmentReferences("M", k))))
    }

    "encrypt AssociateUcrAnswers Answers" in {
      val answers =
        AssociateUcrAnswers(
          mucrOptions = Some(MucrOptions("newMucr", "existingMucr", "add")),
          associateUcr = Some(AssociateUcr(AssociateKind.Mucr, "mucr"))
        )
      val cache = Cache("eori", answers)
      val enc = crypto.encrypt(cache)

      enc mustBe cache.copy(
        eori = k,
        answers = answers.copy(mucrOptions = Some(MucrOptions(k, k, "add")), associateUcr = Some(AssociateUcr(AssociateKind.Mucr, k)))
      )
    }

    "encrypt DisassociateUcrAnswers Answers" in {
      val answers =
        DisassociateUcrAnswers(ucr = Some(DisassociateUcr(DisassociateKind.Mucr, Some("ducr"), Some("mucr"))))
      val cache = Cache("eori", answers)
      val enc = crypto.encrypt(cache)

      enc mustBe cache.copy(eori = k, answers = answers.copy(ucr = Some(DisassociateUcr(DisassociateKind.Mucr, Some(k), Some(k)))))
    }

    "encrypt ShutMucrAnswers Answers" in {
      val answers = ShutMucrAnswers(shutMucr = Some(ShutMucr("mucr")))
      val cache = Cache("eori", answers)
      val enc = crypto.encrypt(cache)

      enc mustBe cache.copy(eori = k, answers = answers.copy(shutMucr = Some(ShutMucr(k))))
    }
  }

  "decrypt()" should {

    val k = UUID.randomUUID().toString
    when(simmetricCrypto.decrypt(any[Crypted]())).thenReturn(PlainText(k))

    "decrypt Arrival Answers" in {
      val answers = ArrivalAnswers(
        consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345")),
        arrivalDetails = Some(ArrivalDetails(Date(LocalDate.now()), Time(LocalTime.now()))),
        location = Some(Location("code"))
      )
      val cache = Cache("eori", answers)
      val enc = crypto.decrypt(cache)

      enc mustBe cache.copy(eori = k, answers = answers.copy(consignmentReferences = Some(ConsignmentReferences("M", k))))
    }

    "decrypt Departure Answers" in {
      val answers = DepartureAnswers(
        consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345")),
        departureDetails = Some(DepartureDetails(Date(LocalDate.now()), Time(LocalTime.now()))),
        location = Some(Location("code")),
        transport = Some(Transport("mode", "id", "nat"))
      )
      val cache = Cache("eori", answers)
      val enc = crypto.decrypt(cache)

      enc mustBe cache.copy(eori = k, answers = answers.copy(consignmentReferences = Some(ConsignmentReferences("M", k))))
    }

    "decrypt AssociateUcrAnswers Answers" in {
      val answers =
        AssociateUcrAnswers(
          mucrOptions = Some(MucrOptions("newMucr", "existingMucr", "add")),
          associateUcr = Some(AssociateUcr(AssociateKind.Mucr, "mucr"))
        )
      val cache = Cache("eori", answers)
      val enc = crypto.decrypt(cache)

      enc mustBe cache.copy(
        eori = k,
        answers = answers.copy(mucrOptions = Some(MucrOptions(k, k, "add")), associateUcr = Some(AssociateUcr(AssociateKind.Mucr, k)))
      )
    }

    "decrypt DisassociateUcrAnswers Answers" in {
      val answers =
        DisassociateUcrAnswers(ucr = Some(DisassociateUcr(DisassociateKind.Mucr, Some("ducr"), Some("mucr"))))
      val cache = Cache("eori", answers)
      val enc = crypto.decrypt(cache)

      enc mustBe cache.copy(eori = k, answers = answers.copy(ucr = Some(DisassociateUcr(DisassociateKind.Mucr, Some(k), Some(k)))))
    }

    "decrypt ShutMucrAnswers Answers" in {
      val answers = ShutMucrAnswers(shutMucr = Some(ShutMucr("mucr")))
      val cache = Cache("eori", answers)
      val enc = crypto.decrypt(cache)

      enc mustBe cache.copy(eori = k, answers = answers.copy(shutMucr = Some(ShutMucr(k))))
    }
  }
}
