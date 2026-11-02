package co.edu.unal.dadm.mova.data.database

import co.edu.unal.dadm.mova.data.dao.UserDao
import co.edu.unal.dadm.mova.data.dao.UserProfileDao

class MovaDatabase {
    fun userDao(): UserDao = UserDao()
    fun userProfileDao(): UserProfileDao = UserProfileDao()
}
