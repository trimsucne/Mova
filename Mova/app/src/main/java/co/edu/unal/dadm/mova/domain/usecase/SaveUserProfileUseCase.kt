package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.domain.model.UserProfile
import co.edu.unal.dadm.mova.domain.repository.ProfileRepository

class SaveUserProfileUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(profile: UserProfile) {
        repository.update(
            profile.userId,
            mapOf(
                "displayName" to profile.displayName,
                "avatarId" to profile.avatarId,
                "age" to profile.age,
                "weightKg" to profile.weightKg,
                "onboardingCompleted" to true
            )
        )
    }
}
