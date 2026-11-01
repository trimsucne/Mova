package co.edu.unal.dadm.mova.presentation.viewmodel.onboarding

import co.edu.unal.dadm.mova.domain.model.Diet
import co.edu.unal.dadm.mova.domain.model.FitnessLevel
import co.edu.unal.dadm.mova.domain.model.Gender
import co.edu.unal.dadm.mova.domain.model.Goal

sealed interface OnboardingEvent {
    data class SetAge(val age: Int) : OnboardingEvent
    data class SetWeight(val weightKg: Double) : OnboardingEvent
    data class SetDiet(val diet: Diet) : OnboardingEvent
    data class SetGender(val gender: Gender) : OnboardingEvent
    data class SetFitnessLevel(val level: FitnessLevel) : OnboardingEvent
    data class SetGoal(val goal: Goal) : OnboardingEvent
    data class SetVoiceNote(val note: String?) : OnboardingEvent
    data class SetAvatar(val avatarId: String) : OnboardingEvent
    data object Complete : OnboardingEvent
}
