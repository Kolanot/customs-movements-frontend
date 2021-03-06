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

package views.components.config
import base.UnitSpec
import forms.DucrPartChiefChoice

class SpecificDateTimeConfigSpec extends UnitSpec with ViewConfigFeaturesSpec {

  "SpecificDateTimeConfig when ileQuery disabled" should {

    val config = new SpecificDateTimeConfig(ileQueryDisabled)

    "return correct back url" in {
      config.backUrl(None) must be(controllers.routes.ConsignmentReferencesController.displayPage())
    }

    "return correct back url when when it is a Ducr Part" in {
      config.backUrl(Some(DucrPartChiefChoice(DucrPartChiefChoice.IsDucrPart))) mustBe controllers.routes.DucrPartDetailsController
        .displayPage()
    }

    "return correct back url when when its not a Ducr Part" in {
      config.backUrl(Some(DucrPartChiefChoice(DucrPartChiefChoice.NotDucrPart))) mustBe controllers.routes.ConsignmentReferencesController
        .displayPage()
    }
  }

  "SpecificDateTimeConfig when ileQuery enabled" should {

    val config = new SpecificDateTimeConfig(ileQueryEnabled)

    "return correct url when query ucr present" in {
      config.backUrl(None) must be(controllers.routes.ChoiceController.displayChoiceForm())
    }

  }
}
