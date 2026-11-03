package co.edu.unal.dadm.mova.data.mapper

import co.edu.unal.dadm.mova.data.entity.GymEntity
import co.edu.unal.dadm.mova.domain.model.Gym

fun GymEntity.toDomain() = Gym(id, name, address, lat, lng)

fun Gym.toEntity() = GymEntity(id, name, address, lat, lng)
