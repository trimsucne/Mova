package co.edu.unal.dadm.mova.data.mapper

import co.edu.unal.dadm.mova.data.entity.WorkoutSessionEntity
import co.edu.unal.dadm.mova.domain.model.WorkoutSessionLog

fun WorkoutSessionEntity.toDomain() =
    WorkoutSessionLog(id, workoutId, workoutTitle, startedAt, durationSec, kcalBurned, completedSets, avgBpm)

fun WorkoutSessionLog.toEntity() =
    WorkoutSessionEntity(id, workoutId, workoutTitle, startedAt, durationSec, kcalBurned, completedSets, avgBpm)
