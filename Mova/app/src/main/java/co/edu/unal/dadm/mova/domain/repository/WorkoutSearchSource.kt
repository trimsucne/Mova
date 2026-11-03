package co.edu.unal.dadm.mova.domain.repository

import co.edu.unal.dadm.mova.domain.model.Searchable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Entrenamientos para Búsqueda (spec §12). Mientras Entrenamiento no exponga observeWorkouts(),
 * se usa NONE; al conectarlo, solo se reemplaza en SearchContainer.
 */
interface WorkoutSearchSource {
    val isAvailable: Boolean
    fun observe(): Flow<List<Searchable>>

    companion object {
        val NONE = object : WorkoutSearchSource {
            override val isAvailable = false
            override fun observe(): Flow<List<Searchable>> = flowOf(emptyList())
        }
    }
}
