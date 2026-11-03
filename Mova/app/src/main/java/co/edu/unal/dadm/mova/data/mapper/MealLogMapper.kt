package co.edu.unal.dadm.mova.data.mapper

import co.edu.unal.dadm.mova.data.entity.MealLogEntity
import co.edu.unal.dadm.mova.data.entity.SyncState
import co.edu.unal.dadm.mova.domain.model.Macros
import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.model.MealSource
import co.edu.unal.dadm.mova.domain.model.MealType
import java.time.Instant
import java.time.LocalDate

fun MealLogEntity.toDomain() = MealLog(
    id = id,
    userId = userId,
    date = LocalDate.parse(date),
    mealType = MealType.valueOf(mealType),
    source = MealSource.valueOf(source),
    mealId = mealId,
    name = name,
    grams = grams,
    macros = Macros(proteinG, carbsG, fatG),
    kcal = kcal,
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt)
)

fun MealLog.toEntity(syncState: SyncState, deleted: Boolean = false) = MealLogEntity(
    id = id,
    userId = userId,
    date = date.toString(),
    mealType = mealType.name,
    source = source.name,
    mealId = mealId,
    name = name,
    grams = grams,
    proteinG = macros.proteinG,
    carbsG = macros.carbsG,
    fatG = macros.fatG,
    kcal = kcal,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
    deleted = deleted,
    syncState = syncState.name
)
