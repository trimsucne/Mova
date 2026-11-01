package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.domain.repository.AssessmentRepository
import co.edu.unal.dadm.mova.domain.repository.AuthRepository
import co.edu.unal.dadm.mova.domain.usecase.SaveAssessmentUseCase
import co.edu.unal.dadm.mova.presentation.viewmodel.onboarding.OnboardingViewModelFactory
import co.edu.unal.dadm.mova.domain.model.Diet
import co.edu.unal.dadm.mova.domain.model.FitnessLevel
import co.edu.unal.dadm.mova.domain.model.Gender
import co.edu.unal.dadm.mova.domain.model.Goal
import co.edu.unal.dadm.mova.presentation.viewmodel.onboarding.OnboardingEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.onboarding.OnboardingViewModel

@Composable
fun AssessmentScreen(
    authRepository: AuthRepository,
    assessmentRepository: AssessmentRepository,
    onComplete: () -> Unit
) {
    val userId = requireNotNull(authRepository.currentUser.value?.id) {
        "Assessment requires an authenticated user"
    }
    val viewModel: OnboardingViewModel = viewModel(
        factory = OnboardingViewModelFactory(SaveAssessmentUseCase(assessmentRepository), userId)
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var step by remember { mutableIntStateOf(0) }

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) onComplete()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        state.error?.let { Text(it) }
        when (step) {
            0 -> AgeStep(state.draft.age, { viewModel.onEvent(OnboardingEvent.SetAge(it)) }) { step++ }
            1 -> DietStep(state.draft.diet, { viewModel.onEvent(OnboardingEvent.SetDiet(it)) }) { step++ }
            2 -> WeightStep(state.draft.weightKg, { viewModel.onEvent(OnboardingEvent.SetWeight(it)) }) { step++ }
            3 -> FitnessLevelStep(state.draft.fitnessLevel, { viewModel.onEvent(OnboardingEvent.SetFitnessLevel(it)) }) { step++ }
            4 -> GenderStep(state.draft.gender, { viewModel.onEvent(OnboardingEvent.SetGender(it)) }) { step++ }
            5 -> GoalStep(state.draft.goal, { viewModel.onEvent(OnboardingEvent.SetGoal(it)) }) { step++ }
            6 -> VoiceStep {
                viewModel.onEvent(OnboardingEvent.SetVoiceNote(null))
                step++
            }
            else -> AvatarStep(state.draft.avatarId, { viewModel.onEvent(OnboardingEvent.SetAvatar(it)) }) {
                viewModel.onEvent(OnboardingEvent.Complete)
            }
        }
    }
}
