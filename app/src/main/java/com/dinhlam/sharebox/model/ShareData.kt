package com.dinhlam.sharebox.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface ShareData : Parcelable {

    @Parcelize
    data class ShareText(val text: String) : ShareData

    @Parcelize
    data class ShareUrl(val url: String) : ShareData

    @Parcelize
    data class ShareImage(val uri: Uri) : ShareData

    @Parcelize
    data class ShareImages(val uris: List<Uri>) : ShareData

    @Parcelize
    data class ShareFile(
        val fileName: String,
        val fileSize: Double,
        val mimeType: String?,
        val uri: Uri
    ) : ShareData

    @Parcelize
    data class ShareCheckList(val checkListDataList: List<CheckListData>) :
        ShareData {
        @Parcelize
        data class CheckListData(
            val title: String,
            val done: Boolean,
            val datetime: Long,
            val reminder: Long,
            val updatedAt: Long
        ) :
            Parcelable
    }

    @Parcelize
    data class ShareNotification(val appName: String, val title: String, val content: String, val deeplink: String?) :
        ShareData
}