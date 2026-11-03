package co.edu.unal.dadm.mova.data.mapper

import co.edu.unal.dadm.mova.data.entity.MealEntity
import co.edu.unal.dadm.mova.domain.model.Diet
import co.edu.unal.dadm.mova.domain.model.Macros
import co.edu.unal.dadm.mova.domain.model.Meal
import co.edu.unal.dadm.mova.domain.model.MealCategory
import co.edu.unal.dadm.mova.domain.model.MealType

fun MealEntity.toDomain() = Meal(
    id = id,
    name = name,
    category = MealCategory.valueOf(category),
    suitableFor = suitableFor.csv().map { MealType.valueOf(it) }.toSet(),
    dietTags = dietTags.csv().map { Diet.valueOf(it) }.toSet(),
    servingGrams = servingGrams,
    macros = Macros(proteinG, carbsG, fatG),
    kcal = kcal,
    prepMinutes = prepMinutes,
    imageRes = imageRes,
    tags = tags.csv()
)

internal fun String.csv(): List<String> = split(',').map { it.trim() }.filter { it.isNotEmpty() }
