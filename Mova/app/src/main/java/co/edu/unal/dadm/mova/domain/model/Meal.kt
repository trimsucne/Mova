package co.edu.unal.dadm.mova.domain.model

data class Meal(
    val id: String,
    val name: String,
    val category: MealCategory,
    val suitableFor: Set<MealType>,
    val dietTags: Set<Diet>,
    val servingGrams: Int,
    val macros: Macros,
    val kcal: Int,
    val prepMinutes: Int,
    val imageRes: String,
    val tags: List<String>
)
