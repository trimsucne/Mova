package co.edu.unal.dadm.mova.data.repository

import co.edu.unal.dadm.mova.data.dao.GymDao
import co.edu.unal.dadm.mova.data.mapper.toDomain
import co.edu.unal.dadm.mova.domain.model.Gym
import co.edu.unal.dadm.mova.domain.repository.PlacesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Gimnasios persistidos en Room (sembrados con datos de muestra; ver FitnessDatabase.seedIfEmpty). */
class PlacesRepositoryImpl(private val dao: GymDao) : PlacesRepository {
    override fun observeGyms(): Flow<List<Gym>> = dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeGym(id: String): Flow<Gym?> = dao.observeById(id).map { it?.toDomain() }
}
