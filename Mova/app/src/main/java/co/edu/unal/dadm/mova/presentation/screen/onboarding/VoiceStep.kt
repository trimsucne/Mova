package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.runtime.Composable

@Composable
fun VoiceStep(onContinue: () -> Unit) {
    AssessmentStep(
        6,
        "AI Vocal Analysis",
        "Voice analysis is optional. Continue without recording for now.",
        true,
        onContinue,
        progressLabel = "Optional setup"
    ) {}
}
