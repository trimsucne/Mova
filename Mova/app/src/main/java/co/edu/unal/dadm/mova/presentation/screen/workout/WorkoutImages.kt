package co.edu.unal.dadm.mova.presentation.screen.workout

import androidx.annotation.DrawableRes
import co.edu.unal.dadm.mova.R

/** Resuelve la clave de imagen del dominio a un drawable (domain no conoce Android). */
@DrawableRes
fun workoutImage(key: String): Int = when (key) {
    "female_athlete" -> R.drawable.img_female_athlete
    else -> R.drawable.img_male_runner
}
