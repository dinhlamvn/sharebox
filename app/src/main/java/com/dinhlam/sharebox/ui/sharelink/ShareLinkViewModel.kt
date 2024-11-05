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

    init {
        getDefaultBox()
    }

    private fun getDefaultBox() {
        suspend {
            boxRepository.findFirst(userHelper.getCurrentUserId())
        }.execute { asyncLoad ->
            copy(currentBox = asyncLoad.data)
        }
    }

    fun setCurrentBoxId(boxId: String) {
        suspend { boxRepository.findOne(boxId) }.execute { asyncLoad ->
            copy(currentBox = asyncLoad.data)
        }
    }

    fun archiveLink(link: String) = getState { state ->
        suspend {
            val shareData = ShareData.ShareUrl(link)
            val isVideoShare = videoHelper.getVideoSource(shareData.url) != null
            shareRepository.insert(
                shareData = shareData,
                shareNote = null,
                shareBoxId = state.currentBox?.boxId,
                shareUserId = userHelper.getCurrentUserId(),
                isVideoShare = isVideoShare
            )!!
        }.execute { asyncLoad ->
            copy(asyncLoadArchive = asyncLoad)
        }
    }
}