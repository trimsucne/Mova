package co.edu.unal.dadm.mova.domain.model

import java.text.Normalizer
import kotlin.math.roundToInt

/** Normalización y puntaje de coincidencia (spec de Búsqueda §3). */
object SearchScorer {
    private val combiningMarks = "\\p{Mn}+".toRegex()
    private val nonAlphanumeric = "[^\\p{L}\\p{N}]+".toRegex()

    fun normalize(text: String): String =
        Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
            .replace(combiningMarks, "")
            .replace(nonAlphanumeric, " ")
            .trim()

    fun score(query: String, item: Searchable): Int? {
        val q = normalize(query)
        if (q.isEmpty()) return null
        val name = normalize(item.title)
        val extras = (listOf(item.category) + item.tags).map(::normalize)
        singleScore(q, name, extras)?.let { return it }
        val words = q.split(' ')
        if (words.size < 2) return null
        val scores = words.map { word -> singleScore(word, name, extras) ?: return null }
        return (scores.sum().toDouble() / scores.size).roundToInt()
    }

    fun rank(query: String, items: List<Searchable>, limit: Int = 30): List<SearchResult> =
        items.mapNotNull { item -> score(query, item)?.let { item.toResult(it) } }
            .sortedWith(compareByDescending<SearchResult> { it.score }.thenBy { normalize(it.title) })
            .take(limit)

    private fun singleScore(q: String, name: String, extras: List<String>): Int? = when {
        name == q -> 100
        name.startsWith(q) -> 85
        name.split(' ').any { it.startsWith(q) } -> 70
        name.contains(q) -> 55
        extras.any { it.contains(q) } -> 40
        else -> null
    }
}
