package com.dinhlam.sharebox.ui.sharelink

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.helper.VideoHelper
import com.dinhlam.sharebox.model.ShareData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShareLinkViewModel @Inject constructor(
    private val boxRepository: BoxRepository,
    private val userHelper: UserHelper,
    private val videoHelper: VideoHelper,
    private val shareRepository: ShareRepository,
) : BaseViewModel<ShareLinkState>(ShareLinkState()) {

    fun archiveLink(link: String, boxId: String) = getState { state ->
        suspend {
            val shareData = ShareData.ShareUrl(link)
            val isVideoShare = videoHelper.getVideoSource(shareData.url) != null
            shareRepository.insert(
                shareData = shareData,
                shareNote = null,
                shareBoxId = boxId,
                shareUserId = userHelper.getCurrentUserId(),
                isVideoShare = isVideoShare
            )!!
        }.execute { asyncLoad ->
            copy(asyncLoadArchive = asyncLoad)
        }
    }

    fun setLinkError(error: String?) = setState {
        copy(linkError = error)
    }
}