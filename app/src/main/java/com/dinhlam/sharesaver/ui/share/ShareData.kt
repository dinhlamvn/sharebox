package com.dinhlam.sharesaver.ui.share

import android.net.Uri
import com.dinhlam.sharesaver.base.BaseViewModel

data class ShareData(
    val shareInfo: ShareData.ShareInfo = ShareInfo.None,
    val isSaveSuccess: Boolean = false
) : BaseViewModel.BaseData {

    sealed class ShareInfo {
        object None : ShareInfo()
        data class ShareText(val text: String?) : ShareInfo()
        data class ShareImage(val uri: Uri) : ShareInfo()
        data class ShareMultipleImage(val uris: List<Uri>) : ShareInfo()
    }
}
