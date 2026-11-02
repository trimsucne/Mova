package co.edu.unal.dadm.mova.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SearchScorerTest {
    private fun item(title: String, category: String = "Grains", tags: List<String> = emptyList(), id: String = title) =
        Searchable(id, SearchResult.Type.MEAL, title, "", null, category, tags)

    private val toast = item("Avocado Toast", tags = listOf("bread"))

    @Test
    fun `normalize removes case, accents and punctuation`() {
        assertEquals("pina colada", SearchScorer.normalize("  Piña-Colada! "))
    }

    @Test
    fun `score table`() {
        assertEquals(100, SearchScorer.score("avocado toast", toast))
        assertEquals(85, SearchScorer.score("avo", toast))
        assertEquals(70, SearchScorer.score("toa", toast))
        assertEquals(55, SearchScorer.score("cado", toast))
        assertEquals(40, SearchScorer.score("grain", toast))
        assertEquals(40, SearchScorer.score("bread", toast))
        assertNull(SearchScorer.score("salmon", toast))
    }

    @Test
    fun `accents and case do not matter in either direction`() {
        assertEquals(85, SearchScorer.score("pina", item("Piña Colada")))
        assertEquals(85, SearchScorer.score("PIÑA", item("Pina Colada")))
    }

    @Test
    fun `multi word query averages each word`() {
        // toast -> 70 (palabra), avocado -> 85 (inicio); (70 + 85) / 2 = 77.5 -> 78
        assertEquals(78, SearchScorer.score("toast avocado", toast))
    }

    @Test
    fun `multi word query needs every word`() {
        assertNull(SearchScorer.score("toast salmon", toast))
    }

    @Test
    fun `rank orders by score then alphabetically and limits`() {
        val items = listOf(item("Toast Bites"), item("Avocado Toast"), item("Toast"), item("French Toast"))
        val ranked = SearchScorer.rank("toast", items)
        assertEquals(listOf("Toast", "Toast Bites", "Avocado Toast", "French Toast"), ranked.map { it.title })
        assertEquals(listOf(100, 85, 70, 70), ranked.map { it.score })

        val many = (1..40).map { item("Toast $it") }
        assertEquals(30, SearchScorer.rank("toast", many).size)
    }
}
