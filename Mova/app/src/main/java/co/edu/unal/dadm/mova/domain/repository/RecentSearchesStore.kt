package co.edu.unal.dadm.mova.domain.repository

import kotlinx.coroutines.flow.Flow

/** Búsquedas recientes (B7). Interno del módulo de Búsqueda. */
interface RecentSearchesStore {
    val recent: Flow<List<String>>
    suspend fun add(query: String)
    suspend fun clear()
}
