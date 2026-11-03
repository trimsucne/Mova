package co.edu.unal.dadm.mova.domain.repository

import co.edu.unal.dadm.mova.domain.model.Gym
import kotlinx.coroutines.flow.Flow

interface PlacesRepository {
    fun observeGyms(): Flow<List<Gym>>
    fun observeGym(id: String): Flow<Gym?>
}
