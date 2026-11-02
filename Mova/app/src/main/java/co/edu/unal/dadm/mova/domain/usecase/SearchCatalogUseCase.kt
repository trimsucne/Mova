package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.domain.model.SearchResult
import co.edu.unal.dadm.mova.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow

class SearchCatalogUseCase(private val repository: SearchRepository) {
    operator fun invoke(query: String, types: Set<SearchResult.Type>): Flow<List<SearchResult>> =
        repository.search(query, types)
}
