package co.edu.unal.dadm.mova.domain.repository

import co.edu.unal.dadm.mova.domain.model.SearchResult
import kotlinx.coroutines.flow.Flow

/** Primeros elementos en orden alfabético, para el estado «Not Found» (spec §3). Interno del módulo. */
fun interface SearchFallbacks {
    fun firstItems(types: Set<SearchResult.Type>, limit: Int): Flow<List<SearchResult>>
}
