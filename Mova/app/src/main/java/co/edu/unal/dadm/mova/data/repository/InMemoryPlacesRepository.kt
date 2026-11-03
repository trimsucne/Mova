package co.edu.unal.dadm.mova.data.repository

import co.edu.unal.dadm.mova.domain.model.Gym
import co.edu.unal.dadm.mova.domain.repository.PlacesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Catalogo local de gimnasios de ejemplo (zona Bogota). Datos de muestra: sustituir
 * por Places API cuando haya clave/cuota.
 */
class InMemoryPlacesRepository(seed: List<Gym> = SAMPLE_GYMS) : PlacesRepository {
    private val gyms = MutableStateFlow(seed)

    override fun observeGyms(): Flow<List<Gym>> = gyms

    override fun observeGym(id: String): Flow<Gym?> =
        gyms.map { list -> list.firstOrNull { it.id == id } }

    companion object {
        val SAMPLE_GYMS = listOf(
            Gym("g1", "Campus Fitness Center", "Ciudad Universitaria, Bogota", 4.6382, -74.0840),
            Gym("g2", "Teusaquillo Gym", "Cra. 24 #39, Bogota", 4.6310, -74.0770),
            Gym("g3", "Chapinero Strength Club", "Calle 63 #11, Bogota", 4.6486, -74.0620),
            Gym("g4", "Salitre Wellness", "Av. Boyaca #53, Bogota", 4.6560, -74.0950),
            Gym("g5", "Parkway Training", "Cra. 24 #45, Bogota", 4.6350, -74.0700)
        )
    }
}
