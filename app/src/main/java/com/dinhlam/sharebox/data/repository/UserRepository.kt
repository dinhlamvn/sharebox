package com.dinhlam.sharebox.data.repository

import android.util.Log
import com.dinhlam.sharebox.data.local.dao.UserDao
import com.dinhlam.sharebox.data.local.entity.User
import com.dinhlam.sharebox.data.model.UserDetail
import com.dinhlam.sharebox.data.repository.mapper.UserToUserDetailMapper
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
    }.onFailure {
        Log.d("DinhLam", it.toString())
    }.getOrDefault(false)


    fun findOne(userId: String): UserDetail? = userId.runCatching {
        userDao.findOne(userId)?.let(::mapUserToUserDetail)
    }.getOrDefault(null)

    private fun mapUserToUserDetail(user: User) = userToUserDetailMapper.map(user)
}