package com.dinhlam.sharebox.ui.sharelink

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.utils.BoxUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShareLinkViewModel @Inject constructor(
    private val boxRepository: BoxRepository,
    private val userHelper: UserHelper,
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
}