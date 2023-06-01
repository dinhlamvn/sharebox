package com.dinhlam.sharebox.data.repository

import com.dinhlam.sharebox.data.local.dao.UserDao
import com.dinhlam.sharebox.data.local.entity.User
import com.dinhlam.sharebox.data.mapper.UserToUserDetailMapper
import com.dinhlam.sharebox.data.model.UserDetail
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val userToUserDetailMapper: UserToUserDetailMapper
) {

    suspend fun insert(user: User): Boolean = user.runCatching {
        userDao.insert(user)
        true
    }.getOrDefault(false)


    suspend fun upsert(user: User): Boolean = user.runCatching {
        userDao.upsert(user)
        true
    }.getOrDefault(false)


    suspend fun findOne(userId: String): UserDetail? = userId.runCatching {
        userDao.findOne(userId)?.let(::mapUserToUserDetail)
    }.getOrDefault(null)

    suspend fun findOneRaw(userId: String): User? = userId.runCatching {
        userDao.findOne(userId)
    }.getOrDefault(null)

    private fun mapUserToUserDetail(user: User) = userToUserDetailMapper.map(user)
}