package co.edu.unal.dadm.mova.presentation.viewmodel.home

import co.edu.unal.dadm.mova.domain.model.SearchResult
import co.edu.unal.dadm.mova.domain.model.SearchScorer
import co.edu.unal.dadm.mova.domain.model.Searchable
import co.edu.unal.dadm.mova.domain.repository.SearchFallbacks
import co.edu.unal.dadm.mova.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class FakeSearchRepository(private val items: List<Searchable>) : SearchRepository, SearchFallbacks {
    var failing = false
    val queries = mutableListOf<Pair<String, Set<SearchResult.Type>>>()

    override fun search(query: String, types: Set<SearchResult.Type>): Flow<List<SearchResult>> = flow {
        queries += query to types
        if (failing) throw IllegalStateException("boom")
        emit(SearchScorer.rank(query, items.filter { it.type in types }))
    }

    override fun suggestions(prefix: String): Flow<List<String>> =
        flowOf(items.map { it.title }.filter { SearchScorer.normalize(it).startsWith(SearchScorer.normalize(prefix)) }.sorted().take(6))

    override fun firstItems(types: Set<SearchResult.Type>, limit: Int): Flow<List<SearchResult>> =
        flowOf(items.filter { it.type in types }.sortedBy { it.title }.take(limit).map { it.toResult(0) })
}
