/*
 * Copyright 2021 HM Revenue & Customs
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

import base.OverridableInjector
import config.DucrPartConfig
import forms.ConsignmentReferences
import models.cache.ArrivalAnswers
import org.jsoup.nodes.Document
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.FormError
import play.api.inject.bind
import views.html.consignment_references

class ConsignmentReferenceViewSpec extends ViewSpec with MockitoSugar with BeforeAndAfterEach {

  private val appConfig = mock[DucrPartConfig]
  private val injector = new OverridableInjector(bind[DucrPartConfig].toInstance(appConfig))

  private implicit val request = journeyRequest(ArrivalAnswers())

  private val page = injector.instanceOf[consignment_references]

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(appConfig.isDucrPartsEnabled).thenReturn(true)
  }

  override def afterEach(): Unit = {
    reset(appConfig)
    super.afterEach()
  }

  "View" should {
    "render title" in {
      page(ConsignmentReferences.form).getTitle must containMessage("consignmentReferences.ARRIVE.question")
    }

    "render heading" in {
      page(ConsignmentReferences.form).getElementById("section-header") must containMessage("consignmentReferences.ARRIVE.heading")
    }

    "render options" in {
      page(ConsignmentReferences.form).getElementsByAttributeValue("for", "reference").first() must containMessage(
        "consignmentReferences.reference.ducr"
      )
      page(ConsignmentReferences.form).getElementsByAttributeValue("for", "reference-2").first() must containMessage(
        "consignmentReferences.reference.mucr"
      )
    }

    "render labels" in {
      page(ConsignmentReferences.form).getElementsByAttributeValue("for", "mucrValue").first() must containMessage("site.inputText.mucr.label")
      page(ConsignmentReferences.form).getElementsByAttributeValue("for", "ducrValue").first() must containMessage("site.inputText.ducr.label")
    }

    "render back button when ducrPart disabled" in {
      when(appConfig.isDucrPartsEnabled).thenReturn(false)
      val backButton = page(ConsignmentReferences.form).getBackButton

      backButton mustBe defined
      backButton.get must haveHref(controllers.routes.ChoiceController.displayChoiceForm())
    }

    "render back button when ducrPart enabled" in {
      when(appConfig.isDucrPartsEnabled).thenReturn(true)
      val backButton = page(ConsignmentReferences.form).getBackButton

      backButton mustBe defined
      backButton.get must haveHref(controllers.routes.DucrPartChiefController.displayPage())
    }

    "render error summary" when {
      "no errors" in {
        page(ConsignmentReferences.form).getErrorSummary mustBe empty
      }

      "some errors" in {
        val view: Document = page(ConsignmentReferences.form.withError(FormError("reference", "consignmentReferences.reference.empty")))

        view must haveGovUkGlobalErrorSummary
        view must haveGovUkFieldError("reference", messages("consignmentReferences.reference.empty"))
      }
    }
  }

}
