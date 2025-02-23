package com.dinhlam.sharebox.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FileDownloadInfo(
    val downloadUrl: String,
    val fileName: String,
    val mimeType: String?
) : Parcelable
