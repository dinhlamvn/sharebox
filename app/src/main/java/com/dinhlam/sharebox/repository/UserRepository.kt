package com.dinhlam.sharebox.repository

import com.dinhlam.sharebox.database.dao.UserDao
import com.dinhlam.sharebox.database.entity.User
import com.dinhlam.sharebox.model.UserDetail
import com.dinhlam.sharebox.repository.mapper.UserToUserDetailMapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val userToUserDetailMapper: UserToUserDetailMapper
) {

    fun insert(user: User): Boolean = user.runCatching {
        userDao.insert(user)
        true
    }.getOrDefault(false)


    fun findOne(userId: String): UserDetail? = userId.runCatching {
        userDao.findOne(userId)?.let(::mapUserToUserDetail)
    }.getOrDefault(null)

    private fun mapUserToUserDetail(user: User) = userToUserDetailMapper.map(user)
}