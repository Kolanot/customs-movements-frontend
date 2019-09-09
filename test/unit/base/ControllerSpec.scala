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

package unit.base

import base.{MockAuthConnector, MockCustomsCacheService}
import testdata.MovementsTestData.newUser
import forms.Choice
import forms.Choice.AllowedChoiceValues.Arrival
import models.requests.{AuthenticatedRequest, JourneyRequest}
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsJson, Request}
import play.api.test.FakeRequest
import unit.mocks.{ErrorHandlerMocks, JourneyActionMocks}
import utils.FakeRequestCSRFSupport._
import utils.Stubs

trait ControllerSpec
    extends UnitSpec with Stubs with MockAuthConnector with MockCustomsCacheService with JourneyActionMocks
    with ErrorHandlerMocks {

  protected val user = newUser("12345")

  private val authenticatedRequest = AuthenticatedRequest(FakeRequest("GET", "").withCSRFToken, user)

  protected def getRequest(): JourneyRequest[AnyContentAsEmpty.type] =
    JourneyRequest(authenticatedRequest, Choice(Arrival))

  protected def postRequest(body: JsValue): Request[AnyContentAsJson] =
    FakeRequest("POST", "")
      .withJsonBody(body)
      .withCSRFToken
}