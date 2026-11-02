package co.edu.unal.dadm.mova.data.dao

import co.edu.unal.dadm.mova.data.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UserDao {
    fun getUserById(id: String): Flow<UserEntity?> = flowOf(null)
    suspend fun insertUser(user: UserEntity) {}
    suspend fun updateUser(user: UserEntity) {}
    suspend fun deleteUser(id: String) {}
}
