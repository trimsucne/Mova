package co.edu.unal.dadm.mova.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey val id: String,
    val type: String,
    val startedAt: Long,
    val durationSec: Int,
    val distanceMeters: Double,
    val kcal: Int,
    val avgBpm: Int?,
    /** Ruta serializada como "lat,lng,ts;lat,lng,ts" (ver ActivityLogMapper). */
    val routeEncoded: String
)
