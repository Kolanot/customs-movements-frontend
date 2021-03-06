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

import config.IleQueryConfig
import forms.DucrPartChiefChoice
import javax.inject.Inject
import play.api.mvc.Call

class DissociateSummaryConfig @Inject()(ileQueryConfig: IleQueryConfig) extends BaseConfig(ileQueryConfig) {

  def backUrl(ducrPartChiefChoice: Option[DucrPartChiefChoice]): Call =
    if (ileQueryConfig.isIleQueryEnabled)
      controllers.routes.ChoiceController.displayChoiceForm()
    else if (ducrPartChiefChoice.exists(_.isDucrPart))
      controllers.routes.DucrPartDetailsController.displayPage()
    else
      controllers.consolidations.routes.DisassociateUcrController.displayPage()
}
