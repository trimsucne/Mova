package co.edu.unal.dadm.mova.data.repository

import co.edu.unal.dadm.mova.domain.model.Meal
import co.edu.unal.dadm.mova.domain.model.SearchResult
import co.edu.unal.dadm.mova.domain.model.SearchScorer
import co.edu.unal.dadm.mova.domain.model.Searchable
import co.edu.unal.dadm.mova.domain.repository.MealRepository
import co.edu.unal.dadm.mova.domain.repository.SearchFallbacks
import co.edu.unal.dadm.mova.domain.repository.SearchRepository
import co.edu.unal.dadm.mova.domain.repository.WorkoutSearchSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/** Búsqueda local sobre los catálogos (spec §3 y §9); los catálogos son pequeños y se puntúan en memoria. */
class SearchRepositoryImpl(
    mealRepository: MealRepository,
    workouts: WorkoutSearchSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : SearchRepository, SearchFallbacks {

    private val items: Flow<List<Searchable>> =
        combine(mealRepository.observeCatalog(), workouts.observe()) { meals, routines ->
            meals.map { it.toSearchable() } + routines
        }

    override fun search(query: String, types: Set<SearchResult.Type>): Flow<List<SearchResult>> =
        items.map { all -> SearchScorer.rank(query, all.filter { it.type in types }) }.flowOn(dispatcher)

    override fun suggestions(prefix: String): Flow<List<String>> {
        val normalized = SearchScorer.normalize(prefix)
        return items.map { all ->
            all.filter { SearchScorer.normalize(it.title).startsWith(normalized) }
                .map { it.title }
                .distinct()
                .sortedBy { SearchScorer.normalize(it) }
                .take(MAX_SUGGESTIONS)
        }.flowOn(dispatcher)
    }

    override fun firstItems(types: Set<SearchResult.Type>, limit: Int): Flow<List<SearchResult>> =
        items.map { all ->
            all.filter { it.type in types }
                .sortedBy { SearchScorer.normalize(it.title) }
                .take(limit)
                .map { it.toResult(score = 0) }
        }.flowOn(dispatcher)

    private fun Meal.toSearchable() = Searchable(
        id = id,
        type = SearchResult.Type.MEAL,
        title = name,
        subtitle = "$kcal kcal · ${category.label}",
        imageRes = imageRes,
        category = category.label,
        tags = tags
    )

    private companion object {
        const val MAX_SUGGESTIONS = 6
    }
}
