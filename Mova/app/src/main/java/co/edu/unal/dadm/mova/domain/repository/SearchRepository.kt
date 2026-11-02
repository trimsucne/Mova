package co.edu.unal.dadm.mova.domain.repository

import co.edu.unal.dadm.mova.domain.model.SearchResult
import kotlinx.coroutines.flow.Flow

/** Contrato congelado (D1). */
interface SearchRepository {
    fun search(query: String, types: Set<SearchResult.Type>): Flow<List<SearchResult>>
    fun suggestions(prefix: String): Flow<List<String>>
}
