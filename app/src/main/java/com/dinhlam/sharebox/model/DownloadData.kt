package com.dinhlam.sharebox.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class DownloadData(
    val id: String,
    val mimeType: String,
    val suffix: String,
    val downloadUrl: String
) : Parcelable
