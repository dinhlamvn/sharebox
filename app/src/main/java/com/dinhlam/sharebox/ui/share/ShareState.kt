package com.dinhlam.sharebox.ui.share

import android.net.Uri
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.database.entity.Folder

data class ShareState(
    val shareInfo: ShareInfo = ShareInfo.None,
    val isSaveSuccess: Boolean = false,
    val folders: List<Folder> = emptyList(),
    val selectedFolder: Folder? = null
) : BaseViewModel.BaseState {

    sealed class ShareInfo(val shareType: String) {
        object None : ShareInfo("none")
        data class ShareText(val text: String?) : ShareInfo("text")
        data class ShareWebLink(val url: String?) : ShareInfo("web-link")
        data class ShareImage(val uri: Uri) : ShareInfo("image")
        data class ShareMultipleImage(val uris: List<Uri>) : ShareInfo("multiple-image")
    }
}
