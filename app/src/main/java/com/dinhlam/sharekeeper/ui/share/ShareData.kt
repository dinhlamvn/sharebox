package com.dinhlam.sharekeeper.ui.share

import android.net.Uri
import com.dinhlam.sharekeeper.base.BaseViewModel

data class ShareData(
    val shareInfo: ShareData.ShareInfo = ShareInfo.None
) : BaseViewModel.BaseData {

    sealed class ShareInfo {
        object None : ShareInfo()
        data class ShareText(val text: String?) : ShareInfo()
        data class ShareImage(val uri: Uri) : ShareInfo()
        data class ShareMultipleImage(val uris: List<Uri>) : ShareInfo()
    }
}
