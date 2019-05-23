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

package controllers

import base.MovementBaseSpec
import forms.{ArrivalDetails, Choice, DepartureDetails, MovementDetails}
import forms.Choice.AllowedChoiceValues
import forms.common.{Date, Time}
import play.api.libs.json.{JsNumber, JsObject, JsString, JsValue}
import play.api.test.Helpers._

class MovementDetailsControllerSpec extends MovementBaseSpec {

  val uri = uriWithContextPath("/movement-details")

  trait SetUp {
    authorizedUser()
  }

  trait ArrivalSetUp extends SetUp {
    withCaching(Choice.choiceId, Some(Choice(AllowedChoiceValues.Arrival)))
  }

  trait DepartureSetUp extends SetUp {
    withCaching(Choice.choiceId, Some(Choice(AllowedChoiceValues.Departure)))
  }

  "Movement Details Controller for arrival" should {

    "return 200 for get request" when {

      "cache is empty" in new ArrivalSetUp {

        withCaching(MovementDetails.formId, None)

        val result = route(app, getRequest(uri)).get

        status(result) must be(OK)
      }

      "cache contains data" in new ArrivalSetUp {

        withCaching(
          MovementDetails.formId,
          Some(ArrivalDetails(Date(Some(10), Some(2), Some(2019)), Some(Time(Some("10"), Some("10")))))
        )

        val result = route(app, getRequest(uri)).get

        status(result) must be(OK)
      }
    }

    "return BadRequest for incorrect form" in new ArrivalSetUp {

      val incorrectForm: JsValue = JsObject(Map("dateOfArrival" -> JsString("")))

      val result = route(app, postRequest(uri, incorrectForm)).get

      status(result) must be(BAD_REQUEST)
    }

    "redirect to location page for correct form" in new ArrivalSetUp {

      withCaching(MovementDetails.formId)

      val correctDate: JsValue = JsObject(Map("day" -> JsNumber(10), "month" -> JsNumber(10), "year" -> JsNumber(2019)))
      val correctTime: JsValue = JsObject(Map("hour" -> JsString("10"), "minute" -> JsString("10")))

      val correctForm: JsValue = JsObject(Map("dateOfArrival" -> correctDate, "timeOfArrival" -> correctTime))

      val result = route(app, postRequest(uri, correctForm)).get
      val headers = result.futureValue.header.headers

      status(result) must be(SEE_OTHER)
      headers.get("Location") must be(Some("/customs-movements/location"))
    }
  }

  "Movement Details Controller for departure" should {

    "return 200 for get request" when {

      "cache is empty" in new DepartureSetUp {

        withCaching(MovementDetails.formId, None)

        val result = route(app, getRequest(uri)).get

        status(result) must be(OK)
      }

      "cache contains data" in new DepartureSetUp {

        withCaching(MovementDetails.formId, Some(forms.DepartureDetails(Date(Some(10), Some(2), Some(2019)))))

        val result = route(app, getRequest(uri)).get

        status(result) must be(OK)
      }
    }

    "return BadRequest for incorrect form" in new DepartureSetUp {

      val incorrectForm: JsValue = JsObject(Map("dateOfDeparture" -> JsString("")))

      val result = route(app, postRequest(uri, incorrectForm)).get

      status(result) must be(BAD_REQUEST)
    }

    "redirect to transport page for correct form" in new DepartureSetUp {

      withCaching(MovementDetails.formId)

      val correctDate: JsValue = JsObject(Map("day" -> JsNumber(10), "month" -> JsNumber(10), "year" -> JsNumber(2019)))

      val correctForm: JsValue = JsObject(Map("dateOfDeparture" -> correctDate))

      val result = route(app, postRequest(uri, correctForm)).get
      val headers = result.futureValue.header.headers

      status(result) must be(SEE_OTHER)
      headers.get("Location") must be(Some("/customs-movements/transport"))
    }
  }
}