package co.edu.unal.dadm.mova.presentation.viewmodel.home

import co.edu.unal.dadm.mova.domain.repository.RecentSearchesStore
import kotlinx.coroutines.flow.MutableStateFlow

class FakeRecentSearches : RecentSearchesStore {
    override val recent = MutableStateFlow<List<String>>(emptyList())
    override suspend fun add(query: String) {
        recent.value = (listOf(query.trim()) + recent.value.filterNot { it.equals(query.trim(), true) }).take(8)
    }
    override suspend fun clear() { recent.value = emptyList() }
}
