package com.dinhlam.sharebox.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BoxDetail(
    val boxId: String,
    val boxName: String,
    val boxDesc: String?,
    val createdBy: String,
    val createdDate: Long,
    val passcode: String?,
    val lastSeen: Long
) : Parcelable {

    val isHasPasscode: Boolean
        get() = !passcode.isNullOrBlank()
}
