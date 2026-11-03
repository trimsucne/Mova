package co.edu.unal.dadm.mova.domain.model

/** Comida o entrenamiento tal como lo indexa Búsqueda. */
data class Searchable(
    val id: String,
    val type: SearchResult.Type,
    val title: String,
    val subtitle: String,
    val imageRes: String?,
    val category: String,
    val tags: List<String>
) {
    fun toResult(score: Int) = SearchResult(id, type, title, subtitle, score, imageRes)
}
