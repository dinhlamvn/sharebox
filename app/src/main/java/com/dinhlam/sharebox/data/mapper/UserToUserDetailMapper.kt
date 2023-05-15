package com.dinhlam.sharebox.data.mapper

import com.dinhlam.sharebox.data.local.entity.User
import com.dinhlam.sharebox.data.model.UserDetail
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserToUserDetailMapper @Inject constructor(
    private val gson: Gson
) {
    fun map(user: User): UserDetail {
        return UserDetail(
            user.userId, user.name, user.avatar, user.level, user.drama, user.createdAt
        )
    }
}