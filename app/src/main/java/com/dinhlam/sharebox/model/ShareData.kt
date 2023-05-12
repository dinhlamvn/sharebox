package com.dinhlam.sharebox.model

import android.net.Uri

sealed class ShareData(val shareType: String) {
    object None : ShareData(ShareType.UNKNOWN.type)
    data class ShareText(val text: String?) : ShareData(ShareType.TEXT.type)
    data class ShareUrl(val url: String?) : ShareData(ShareType.URL.type)
    data class ShareImage(val uri: Uri) : ShareData(ShareType.IMAGE.type)
    data class ShareImages(val uris: List<Uri>) :
        ShareData(ShareType.IMAGES.type)
}