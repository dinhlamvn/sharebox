package com.dinhlam.sharebox.ui.sharelink

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.helper.VideoHelper
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.utils.BoxUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShareLinkViewModel @Inject constructor(
    private val boxRepository: BoxRepository,
    private val userHelper: UserHelper,
    private val videoHelper: VideoHelper,
    private val shareRepository: ShareRepository,
) : BaseViewModel<ShareLinkState>(ShareLinkState()) {

    fun getDefaultBox(hasShareLink: Boolean, block: (() -> Unit)? = null) {
        suspend {
            val boxDetail = boxRepository.findFirst(userHelper.getCurrentUserId())
            if (boxDetail == null && hasShareLink) {
                val boxId = BoxUtils.createBoxId("${userHelper.getCurrentUserId()}-webpage")
                boxRepository.insert(
                    boxId,
                    "Web",
                    "Archive web page",
                    userHelper.getCurrentUserId(),
                    nowUTCTimeInMillis()
                )

                boxRepository.findOne(boxId)
            } else boxDetail
        }.execute { asyncLoad ->
            copy(currentBox = asyncLoad.data)
        }.invokeOnCompletion {
            block?.invoke()
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