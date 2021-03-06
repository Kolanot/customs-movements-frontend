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

package controllers.consolidations

import config.IleQueryConfig
import controllers.ControllerLayerSpec
import controllers.storage.FlashKeys
import forms.UcrType._
import forms._
import models.ReturnToStartException
import models.cache.{AssociateUcrAnswers, JourneyType}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.SubmissionService
import views.html.associateucr.{associate_ucr_summary, associate_ucr_summary_no_change}

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class AssociateUcrSummaryControllerSpec extends ControllerLayerSpec with ScalaFutures with IntegrationPatience {

  private val submissionService = mock[SubmissionService]
  private val mockAssociateDucrSummaryPage = mock[associate_ucr_summary]
  private val mockAssociateDucrSummaryNoChangePage = mock[associate_ucr_summary_no_change]

  private val ileQueryConfig = mock[IleQueryConfig]

  private def controller(answers: AssociateUcrAnswers) =
    new AssociateUcrSummaryController(
      SuccessfulAuth(),
      ValidJourney(answers),
      stubMessagesControllerComponents(),
      submissionService,
      ileQueryConfig,
      mockAssociateDucrSummaryPage,
      mockAssociateDucrSummaryNoChangePage
    )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(submissionService, mockAssociateDucrSummaryPage)
    when(mockAssociateDucrSummaryPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockAssociateDucrSummaryNoChangePage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(submissionService, mockAssociateDucrSummaryPage, mockAssociateDucrSummaryNoChangePage, ileQueryConfig)

    super.afterEach()
  }

  private def theResponseDataIleQueryDisabled: (AssociateUcr, String) = {
    val associateDucrCaptor = ArgumentCaptor.forClass(classOf[AssociateUcr])
    val mucrOptionsCaptor = ArgumentCaptor.forClass(classOf[String])
    verify(mockAssociateDucrSummaryPage).apply(associateDucrCaptor.capture(), mucrOptionsCaptor.capture())(any(), any())
    (associateDucrCaptor.getValue, mucrOptionsCaptor.getValue)
  }

  private def theResponseDataIleQueryEnabled: (String, String, UcrType) = {
    val consignmentRefCaptor = ArgumentCaptor.forClass(classOf[String])
    val associateWithCaptor = ArgumentCaptor.forClass(classOf[String])
    val associateKindCaptor = ArgumentCaptor.forClass(classOf[UcrType])
    verify(mockAssociateDucrSummaryNoChangePage).apply(
      consignmentRefCaptor.capture(),
      associateWithCaptor.capture(),
      associateKindCaptor.capture(),
      any()
    )(any(), any())
    (consignmentRefCaptor.getValue, associateWithCaptor.getValue, associateKindCaptor.getValue)
  }

  private val mucrOptions = MucrOptions("MUCR")
  private val associateUcr = AssociateUcr(Ducr, "DUCR")

  "Associate Ducr Summary Controller on displayPage" should {

    "return 200 (OK)" when {

      "display page is invoked with data in cache when ileQuery disabled" in {
        when(ileQueryConfig.isIleQueryEnabled) thenReturn false
        val result = controller(AssociateUcrAnswers(None, Some(mucrOptions), Some(associateUcr))).displayPage()(getRequest())

        status(result) mustBe OK
        verify(mockAssociateDucrSummaryPage).apply(any(), any())(any(), any())

        val (viewUCR, viewOptions) = theResponseDataIleQueryDisabled
        viewUCR.ucr mustBe "DUCR"
        viewOptions mustBe "MUCR"
      }

      "display page when queried ducr" in {
        when(ileQueryConfig.isIleQueryEnabled) thenReturn true
        val result =
          controller(AssociateUcrAnswers(None, Some(MucrOptions("MUCR")), Some(AssociateUcr(Ducr, "Queried DUCR")))).displayPage()(getRequest())

        status(result) mustBe OK
        verify(mockAssociateDucrSummaryNoChangePage).apply(any(), any(), any(), any())(any(), any())

        val (consignmentRef, associateWith, associateKind) = theResponseDataIleQueryEnabled
        consignmentRef mustBe "Queried DUCR"
        associateWith mustBe "MUCR"
        associateKind mustBe UcrType.Mucr
      }

      "display page when queried mucr and 'Associate this consignment to another'" in {
        when(ileQueryConfig.isIleQueryEnabled) thenReturn true
        val result = controller(
          AssociateUcrAnswers(
            Some(ManageMucrChoice(ManageMucrChoice.AssociateThisMucr)),
            Some(MucrOptions("MUCR")),
            Some(AssociateUcr(Mucr, "Queried MUCR"))
          )
        ).displayPage()(getRequest())

        status(result) mustBe OK
        verify(mockAssociateDucrSummaryNoChangePage).apply(any(), any(), any(), any())(any(), any())

        val (consignmentRef, associateWith, associateKind) = theResponseDataIleQueryEnabled
        consignmentRef mustBe "Queried MUCR"
        associateWith mustBe "MUCR"
        associateKind mustBe UcrType.Mucr
      }

      "display page when queried mucr and 'Associate another consignment to this one'" in {
        when(ileQueryConfig.isIleQueryEnabled) thenReturn true
        val result = controller(
          AssociateUcrAnswers(
            Some(ManageMucrChoice(ManageMucrChoice.AssociateAnotherMucr)),
            Some(MucrOptions("Queried MUCR")),
            Some(AssociateUcr(Ducr, "DUCR"))
          )
        ).displayPage()(getRequest())

        status(result) mustBe OK
        verify(mockAssociateDucrSummaryNoChangePage).apply(any(), any(), any(), any())(any(), any())

        val (consignmentRef, associateWith, associateKind) = theResponseDataIleQueryEnabled
        consignmentRef mustBe "Queried MUCR"
        associateWith mustBe "DUCR"
        associateKind mustBe UcrType.Ducr
      }
    }

    "throw an ReturnToStartException exception" when {

      "Mucr Options is missing during displaying page" in {
        intercept[RuntimeException] {
          await(controller(AssociateUcrAnswers(mucrOptions = None, associateUcr = Some(associateUcr))).displayPage()(getRequest()))
        } mustBe ReturnToStartException
      }

      "Associate Ducr is missing during displaying page" in {
        intercept[RuntimeException] {
          await(controller(AssociateUcrAnswers(mucrOptions = Some(mucrOptions), associateUcr = None)).displayPage()(getRequest()))
        } mustBe ReturnToStartException
      }
    }
  }

  "Associate Ducr Summary Controller on submit" when {

    "everything works correctly" should {

      "call SubmissionService" in {
        when(submissionService.submit(any(), any[AssociateUcrAnswers])(any())).thenReturn(Future.successful((): Unit))
        val cachedAnswers = AssociateUcrAnswers(mucrOptions = Some(mucrOptions), associateUcr = Some(associateUcr))

        controller(cachedAnswers).submit()(postRequest()).futureValue

        val expectedEori = SuccessfulAuth().operator.eori
        verify(submissionService).submit(meq(expectedEori), meq(cachedAnswers))(any())
      }

      "return SEE_OTHER (303) that redirects to AssociateUcrConfirmation" in {
        when(submissionService.submit(any(), any[AssociateUcrAnswers])(any())).thenReturn(Future.successful((): Unit))

        val result =
          controller(AssociateUcrAnswers(mucrOptions = Some(mucrOptions), associateUcr = Some(associateUcr))).submit()(postRequest(Json.obj()))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.consolidations.routes.AssociateUcrConfirmationController.displayPage().url)
      }

      "return response with Movement Type in flash" in {
        when(submissionService.submit(any(), any[AssociateUcrAnswers])(any())).thenReturn(Future.successful((): Unit))

        val result =
          controller(AssociateUcrAnswers(mucrOptions = Some(mucrOptions), associateUcr = Some(associateUcr))).submit()(postRequest(Json.obj()))

        flash(result).get(FlashKeys.MOVEMENT_TYPE) mustBe Some(JourneyType.ASSOCIATE_UCR.toString)
      }
    }
  }
}
