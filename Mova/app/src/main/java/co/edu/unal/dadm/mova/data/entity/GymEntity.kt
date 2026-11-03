package co.edu.unal.dadm.mova.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gyms")
data class GymEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double
)
