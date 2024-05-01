package com.dinhlam.sharebox.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserDetail(
    val id: String,
    val name: String,
    val avatar: String,
    val level: Int,
    val drama: Int,
    val createdAt: Long,
    val joinDate: Long,
) : Parcelable
