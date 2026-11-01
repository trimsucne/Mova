package co.edu.unal.dadm.mova.data.repository

import co.edu.unal.dadm.mova.data.remote.FirestoreUserDataSource
import co.edu.unal.dadm.mova.domain.model.Diet
import co.edu.unal.dadm.mova.domain.model.FitnessLevel
import co.edu.unal.dadm.mova.domain.model.Gender
import co.edu.unal.dadm.mova.domain.model.Goal
import co.edu.unal.dadm.mova.domain.model.UserProfile
import co.edu.unal.dadm.mova.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow

class ProfileRepositoryImpl(
    private val dataSource: FirestoreUserDataSource = FirestoreUserDataSource()
) : ProfileRepository {
    override fun observe(userId: String): Flow<UserProfile?> = dataSource.observeUser(userId)
    override suspend fun update(userId: String, changes: Map<String, Any?>) = dataSource.updateUser(userId, changes)
}
