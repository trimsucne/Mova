package co.edu.unal.dadm.mova.domain.model

data class OnboardingDraft(
    val age: Int? = null,
    val weightKg: Double? = null,
    val diet: Diet? = null,
    val gender: Gender? = null,
    val fitnessLevel: FitnessLevel? = null,
    val goal: Goal? = null,
    val voiceNote: String? = null,
    val avatarId: String? = null
)
