package com.dinhlam.sharesaver.ui.share

import android.net.Uri
import com.dinhlam.sharesaver.base.BaseViewModel
import com.dinhlam.sharesaver.database.entity.Folder

data class ShareData(
    val shareInfo: ShareInfo = ShareInfo.None,
    val isSaveSuccess: Boolean = false,
    val selectedFolder: Folder? = null
) : BaseViewModel.BaseData {

    sealed class ShareInfo(val shareType: String) {
        object None : ShareInfo("none")
        data class ShareText(val text: String?) : ShareInfo("text")
        data class ShareWebLink(val url: String?) : ShareInfo("web-link")
        data class ShareImage(val uri: Uri) : ShareInfo("image")
        data class ShareMultipleImage(val uris: List<Uri>) : ShareInfo("multiple-image")
    }
}
