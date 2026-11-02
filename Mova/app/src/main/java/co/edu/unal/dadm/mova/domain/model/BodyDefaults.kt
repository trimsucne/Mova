package co.edu.unal.dadm.mova.domain.model

/** Altura usada cuando el usuario no la ingresó (spec de Nutrición §13.4). */
fun defaultHeightCm(gender: Gender?): Int = when (gender) {
    Gender.MALE -> 170
    Gender.FEMALE -> 160
    Gender.PREFER_NOT_TO_SAY, null -> 165
}
