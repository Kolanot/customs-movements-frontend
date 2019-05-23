/*
 * Copyright 2019 HM Revenue & Customs
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

package controllers.util

import base.BaseSpec
import forms.Choice
import forms.Choice.AllowedChoiceValues.Arrival
import models.SignedInUser
import models.requests.{AuthenticatedRequest, JourneyRequest}
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.{Enrolment, Enrolments}

class CacheIdGeneratorSpec extends BaseSpec {

  val userEori = "eori"
  val userChoice = Arrival

  val fakeRequest = FakeRequest("", "")
  val user = SignedInUser(userEori, Enrolments(Set.empty[Enrolment]))

  val authenticatedRequest = AuthenticatedRequest(fakeRequest, user)
  val journeyRequest = JourneyRequest(authenticatedRequest, Choice(userChoice))

  "Cache Id Generator" should {

    "return correct eori cache Id" in {

      CacheIdGenerator.eoriCacheId()(journeyRequest) must be(userEori)
    }

    "return correct cache Id" in {

      CacheIdGenerator.cacheId()(authenticatedRequest) must be(userEori)
    }

    "return correct movement cache Id" in {

      CacheIdGenerator.movementCacheId()(journeyRequest) must be(s"$userChoice-$userEori")
    }
  }
}