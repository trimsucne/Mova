package co.edu.unal.dadm.mova.data.database

import co.edu.unal.dadm.mova.data.entity.MealEntity
import co.edu.unal.dadm.mova.domain.model.Macros
import org.json.JSONArray

object MealSeedParser {
    fun parse(json: String): List<MealEntity> {
        val array = JSONArray(json)
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            val protein = item.getDouble("proteinG")
            val carbs = item.getDouble("carbsG")
            val fat = item.getDouble("fatG")
            MealEntity(
                id = item.getString("id"),
                name = item.getString("name"),
                category = item.getString("category"),
                suitableFor = item.getJSONArray("suitableFor").strings().joinToString(","),
                dietTags = item.getJSONArray("dietTags").strings().joinToString(","),
                servingGrams = item.getInt("servingGrams"),
                proteinG = protein,
                carbsG = carbs,
                fatG = fat,
                kcal = Macros(protein, carbs, fat).kcal,
                prepMinutes = item.getInt("prepMinutes"),
                imageRes = item.getString("imageRes"),
                tags = item.getJSONArray("tags").strings().joinToString(",")
            )
        }
    }

    private fun JSONArray.strings(): List<String> = (0 until length()).map { getString(it) }
}
