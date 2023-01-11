package com.dinhlam.sharebox.ui.share

import android.net.Uri
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.database.entity.Folder
import com.dinhlam.sharebox.model.ShareType

data class ShareState(
    val shareInfo: ShareInfo = ShareInfo.None,
    val isSaveSuccess: Boolean = false,
    val folders: List<Folder> = emptyList(),
    val selectedFolder: Folder? = null
) : BaseViewModel.BaseState {

    sealed class ShareInfo(val shareType: String) {
        object None : ShareInfo(ShareType.UNKNOWN.type)
        data class ShareText(val text: String?) : ShareInfo(ShareType.TEXT.type)
        data class ShareWebLink(val url: String?) : ShareInfo(ShareType.WEB.type)
        data class ShareImage(val uri: Uri) : ShareInfo(ShareType.IMAGE.type)
        data class ShareMultipleImage(val uris: List<Uri>) : ShareInfo(ShareType.MULTIPLE_IMAGE.type)
    }
}
