package com.dinhlam.sharebox.model

import android.net.Uri

sealed interface ShareData {
    data class ShareText(val text: String) : ShareData
    data class ShareUrl(val url: String) : ShareData
    data class ShareImage(val uri: Uri) : ShareData
    data class ShareImages(val uris: List<Uri>) : ShareData
}