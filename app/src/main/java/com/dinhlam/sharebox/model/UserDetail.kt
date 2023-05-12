package com.dinhlam.sharebox.model

data class UserDetail(
    val id: String,
    val name: String,
    val avatar: String,
    val level: Int,
    val drama: Int,
    val createdAt: Long
)
