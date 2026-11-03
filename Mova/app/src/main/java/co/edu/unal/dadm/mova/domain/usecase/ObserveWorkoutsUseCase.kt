package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.domain.model.Workout
import co.edu.unal.dadm.mova.domain.model.WorkoutCategory
import co.edu.unal.dadm.mova.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow

class ObserveWorkoutsUseCase(private val repository: WorkoutRepository) {
    operator fun invoke(category: WorkoutCategory): Flow<List<Workout>> =
        repository.observeByCategory(category)
}
