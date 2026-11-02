package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.domain.repository.AssessmentRepository
import co.edu.unal.dadm.mova.domain.repository.AuthRepository
import co.edu.unal.dadm.mova.domain.usecase.SaveAssessmentUseCase
import co.edu.unal.dadm.mova.presentation.viewmodel.onboarding.OnboardingEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.onboarding.OnboardingViewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.onboarding.OnboardingViewModelFactory

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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        state.error?.let {
            Text(
                text = it,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        when (step) {
            0 -> AgeStep(
                age = state.draft.age,
                onAgeChanged = { viewModel.onEvent(OnboardingEvent.SetAge(it)) },
                onContinue = { step++ }
            )
            1 -> WeightStep(
                weightKg = state.draft.weightKg,
                onWeightChanged = { viewModel.onEvent(OnboardingEvent.SetWeight(it)) },
                onContinue = { step++ },
                onBack = { step-- }
            )
            2 -> HeightStep(
                heightCm = state.draft.heightCm,
                onHeightChanged = { viewModel.onEvent(OnboardingEvent.SetHeight(it)) },
                onContinue = { step++ },
                onBack = { step-- }
            )
            3 -> FitnessLevelStep(
                selected = state.draft.fitnessLevel,
                onSelected = { viewModel.onEvent(OnboardingEvent.SetFitnessLevel(it)) },
                onContinue = { step++ },
                onBack = { step-- }
            )
            4 -> GenderStep(
                selected = state.draft.gender,
                onSelected = { viewModel.onEvent(OnboardingEvent.SetGender(it)) },
                onContinue = { step++ },
                onBack = { step-- }
            )
            5 -> VoiceStep(
                onContinue = {
                    viewModel.onEvent(OnboardingEvent.SetVoiceNote(null))
                    step++
                },
                onBack = { step-- }
            )
            6 -> GoalStep(
                selected = state.draft.goal,
                onSelected = { viewModel.onEvent(OnboardingEvent.SetGoal(it)) },
                onContinue = { step++ },
                onBack = { step-- }
            )
            7 -> AvatarStep(
                selected = state.draft.avatarId,
                onSelected = { viewModel.onEvent(OnboardingEvent.SetAvatar(it)) },
                onContinue = { step++ },
                onBack = { step-- }
            )
            else -> DietStep(
                selected = state.draft.diet,
                onSelected = { viewModel.onEvent(OnboardingEvent.SetDiet(it)) },
                onContinue = { viewModel.onEvent(OnboardingEvent.Complete) },
                onBack = { step-- }
            )
        }
    }
}
