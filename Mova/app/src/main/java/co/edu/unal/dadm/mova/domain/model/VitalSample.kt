package co.edu.unal.dadm.mova.domain.model

data class VitalSample(
    val bpm: Int,
    val systolic: Int,
    val spo2: Int,
    val timestamp: Long
)
