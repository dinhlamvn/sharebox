package com.dinhlam.sharebox.repository

import com.dinhlam.sharebox.database.dao.UserDao
import com.dinhlam.sharebox.database.entity.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {

    fun insert(user: User): Boolean = user.runCatching {
        userDao.insert(user)
        true
    }.getOrDefault(false)


    fun findOne(userId: String): User? = userId.runCatching {
        userDao.findOne(userId)
    }.getOrDefault(null)
}