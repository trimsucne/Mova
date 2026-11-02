package co.edu.unal.dadm.mova.presentation.viewmodel.onboarding

import co.edu.unal.dadm.mova.domain.model.Diet
import co.edu.unal.dadm.mova.domain.model.FitnessLevel
import co.edu.unal.dadm.mova.domain.model.Gender
import co.edu.unal.dadm.mova.domain.model.Goal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingViewModelTest {
    @Test
    fun `completion rejects incomplete assessment`() {
        val viewModel = OnboardingViewModel()

        viewModel.onEvent(OnboardingEvent.Complete)

        assertFalse(viewModel.uiState.value.isComplete)
        assertTrue(viewModel.uiState.value.error != null)
    }

    @Test
    fun `completion accepts required mockup answers`() {
        val viewModel = OnboardingViewModel()
        listOf(
            OnboardingEvent.SetAge(25),
            OnboardingEvent.SetDiet(Diet.VEGETARIAN),
            OnboardingEvent.SetWeight(70.0),
            OnboardingEvent.SetFitnessLevel(FitnessLevel.INTERMEDIATE),
            OnboardingEvent.SetGender(Gender.PREFER_NOT_TO_SAY),
            OnboardingEvent.SetGoal(Goal.AI_COACH),
            OnboardingEvent.SetAvatar("avatar_1")
        ).forEach(viewModel::onEvent)

        viewModel.onEvent(OnboardingEvent.Complete)

        assertTrue(viewModel.uiState.value.isComplete)
    }

    // Sin SaveAssessmentUseCase ni userId: Complete no lanza corrutinas.
    private fun complete(vararg events: OnboardingEvent) = OnboardingViewModel().run {
        events.forEach(::onEvent)
        onEvent(OnboardingEvent.Complete)
        uiState.value.draft
    }

    @Test
    fun `keeps the chosen height`() {
        assertEquals(182, complete(OnboardingEvent.SetHeight(182)).heightCm)
    }

    @Test
    fun `default height depends on gender`() {
        assertEquals(160, complete(OnboardingEvent.SetGender(Gender.FEMALE)).heightCm)
        assertEquals(165, complete(OnboardingEvent.SetGender(Gender.PREFER_NOT_TO_SAY)).heightCm)
        // Sin género, el onboarding asigna MALE -> 170
        assertEquals(170, complete().heightCm)
    }

    @Test
    fun `diet stays null when not chosen`() {
        assertNull(complete().diet)
    }

    @Test
    fun `chosen diet is kept`() {
        assertEquals(Diet.VEGETARIAN, complete(OnboardingEvent.SetDiet(Diet.VEGETARIAN)).diet)
    }
}
