package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.domain.model.SearchResult
import co.edu.unal.dadm.mova.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchCatalogUseCaseTest {
    @Test
    fun `delegates query and types`() = runTest {
        var received: Pair<String, Set<SearchResult.Type>>? = null
        val repository = object : SearchRepository {
            override fun search(query: String, types: Set<SearchResult.Type>): Flow<List<SearchResult>> {
                received = query to types
                return flowOf(emptyList())
            }
            override fun suggestions(prefix: String): Flow<List<String>> = flowOf(emptyList())
        }
        SearchCatalogUseCase(repository)("toast", setOf(SearchResult.Type.MEAL)).first()
        assertEquals("toast" to setOf(SearchResult.Type.MEAL), received)
    }
}
