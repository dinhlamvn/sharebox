package com.dinhlam.sharekeeper.ui.share

import com.dinhlam.sharekeeper.base.BaseViewModel

class ShareViewModel : BaseViewModel<ShareData>(ShareData()) {

    fun setShareInfo(shareInfo: ShareData.ShareInfo) = setData {
        copy(shareInfo = shareInfo)
    }
}