package co.edu.unal.dadm.mova.presentation.viewmodel.onboarding

import co.edu.unal.dadm.mova.domain.model.Diet
import co.edu.unal.dadm.mova.domain.model.FitnessLevel
import co.edu.unal.dadm.mova.domain.model.Gender
import co.edu.unal.dadm.mova.domain.model.Goal
import org.junit.Assert.assertFalse
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
}
