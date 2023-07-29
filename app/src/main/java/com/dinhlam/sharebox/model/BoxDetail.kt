package com.dinhlam.sharebox.model

data class BoxDetail(
    val boxId: String,
    val boxName: String,
    val boxDesc: String?,
    val createdUser: UserDetail,
    val createdDate: Long,
    val passcode: String?,
    val lastSeen: Long
)
