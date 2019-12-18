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

package views

import java.text.DecimalFormat
import java.time.{LocalDate, LocalTime}

import base.Injector
import forms.common.{Date, Time}
import forms.{ConsignmentReferences, DepartureDetails}
import models.cache.ArrivalAnswers
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.Html
import testdata.CommonTestData.correctUcr
import testdata.MovementsTestData
import views.html.departure_details

class DepartureDetailsViewSpec extends ViewSpec with Injector {

  private implicit val request = journeyRequest(ArrivalAnswers())
  private val movementDetails = MovementsTestData.movementDetails
  private val page = instanceOf[departure_details]

  private val consignmentReferences = "M-ref"
  private def createView(form: Form[DepartureDetails]): Html = page(form, consignmentReferences)(request, messages)

  private def convertIntoTwoDigitFormat(input: Int): String = {
    val formatter = new DecimalFormat("00")
    formatter.format(input)
  }

  private def convertIntoFourDigitFormat(input: Int): String = {
    val formatter = new DecimalFormat("0000")
    formatter.format(input)
  }

  "ArrivalDetails View" when {

    "provided with empty form" should {
      val emptyView = createView(movementDetails.departureForm())

      "have title" in {
        emptyView.getTitle must containMessage("departureDetails.header")
      }

      "have 'Back' button" in {
        val backButton = emptyView.getBackButton

        backButton mustBe defined
        backButton.get must haveHref(controllers.routes.ConsignmentReferencesController.displayPage())
      }

      "have section header" in {
        emptyView.getElementById("section-header") must containMessage("departureDetails.sectionHeading", consignmentReferences)
      }

      "have heading" in {
        emptyView.getElementById("title") must containMessage("departureDetails.header")
      }

      "have date section" which {

        "contains label" in {
          import scala.collection.JavaConversions._

          emptyView.getElementsByTag("legend").exists { elem =>
            elem.text() == messages("departureDetails.date.question")
          }
        }

        "contains hint" in {
          emptyView.getElementById("dateOfDeparture-hint") must containMessage("departureDetails.date.hint")
        }

        "contains input for day" in {
          emptyView.getElementsByAttributeValue("for", "dateOfDeparture_day").first() must containMessage("date.day")
          emptyView.getElementById("dateOfDeparture_day").`val`() mustBe empty
        }

        "contains input for month" in {
          emptyView.getElementsByAttributeValue("for", "dateOfDeparture_month").first() must containMessage("date.month")
          emptyView.getElementById("dateOfDeparture_month").`val`() mustBe empty
        }

        "contains input for year" in {
          emptyView.getElementsByAttributeValue("for", "dateOfDeparture_year").first() must containMessage("date.year")
          emptyView.getElementById("dateOfDeparture_year").`val`() mustBe empty
        }
      }

      "have time section" which {

        "contains label" in {
          import scala.collection.JavaConversions._

          emptyView.getElementsByTag("legend").exists { elem =>
            elem.text() == messages("departureDetails.time.question")
          }
        }

        "contains hint" in {
          emptyView.getElementById("timeOfDeparture-hint") must containMessage("departureDetails.time.hint")
        }

        "contains input for hour" in {
          emptyView.getElementsByAttributeValue("for", "timeOfDeparture_hour").first() must containMessage("time.hour")
          emptyView.getElementById("timeOfDeparture_hour").`val`() mustBe empty
        }

        "contains input for minute" in {
          emptyView.getElementsByAttributeValue("for", "timeOfDeparture_minute").first() must containMessage("time.minute")
          emptyView.getElementById("timeOfDeparture_minute").`val`() mustBe empty
        }
      }

      "have 'Continue' button" in {
        emptyView.getElementsByClass("govuk-button").first() must containMessage("site.continue")
      }
    }

    "provided with form containing data" should {
      val date = LocalDate.now().minusDays(1)
      val time = LocalTime.of(1, 2)
      val viewWithData = createView(movementDetails.departureForm().fill(DepartureDetails(Date(date), Time(time))))

      "have value in day field" in {
        viewWithData.getElementById("dateOfDeparture_day").`val`() mustBe convertIntoTwoDigitFormat(date.getDayOfMonth)
      }

      "have value in month field" in {
        viewWithData.getElementById("dateOfDeparture_month").`val`() mustBe convertIntoTwoDigitFormat(date.getMonthValue)
      }

      "have value in year field" in {
        viewWithData.getElementById("dateOfDeparture_year").`val`() mustBe convertIntoFourDigitFormat(date.getYear)
      }

      "have value in hour field" in {
        viewWithData.getElementById("timeOfDeparture_hour").`val`() mustBe convertIntoTwoDigitFormat(time.getHour)
      }

      "have value in minute field" in {
        viewWithData.getElementById("timeOfDeparture_minute").`val`() mustBe convertIntoTwoDigitFormat(time.getMinute)
      }
    }

    "provided with Date error" should {
      val viewWithDateError: Document = createView(movementDetails.departureForm().withError("dateOfDeparture", "date.error.invalid"))

      "have error summary" in {
        viewWithDateError must haveGovUkGlobalErrorSummary
      }

      "have field error for Date" in {
        viewWithDateError must haveGovUkFieldError("dateOfDeparture", messages("date.error.invalid"))
      }
    }

    "provided with Time error" should {
      val viewWithTimeError: Document = createView(movementDetails.departureForm().withError("timeOfDeparture", "time.error.invalid"))

      "have error summary" in {
        viewWithTimeError must haveGovUkGlobalErrorSummary
      }

      "have field error for Time" in {
        viewWithTimeError must haveGovUkFieldError("timeOfDeparture", messages("time.error.invalid"))
      }
    }
  }

}