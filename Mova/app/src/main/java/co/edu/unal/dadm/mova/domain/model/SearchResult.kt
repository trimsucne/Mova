package co.edu.unal.dadm.mova.domain.model

data class SearchResult(
    val id: String,
    val type: Type,
    val title: String,
    val subtitle: String,
    /** «% Match», de 0 a 100. */
    val score: Int,
    val imageRes: String?
) {
    enum class Type { MEAL, WORKOUT }
}
