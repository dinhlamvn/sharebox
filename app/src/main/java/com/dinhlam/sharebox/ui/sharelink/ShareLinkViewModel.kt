package com.dinhlam.sharebox.ui.sharelink

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.helper.UserHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShareLinkViewModel @Inject constructor(
    private val boxRepository: BoxRepository, private val userHelper: UserHelper
) : BaseViewModel<ShareLinkState>(ShareLinkState()) {

    init {
        getFirstBox()
    }

    fun getFirstBox(block: (() -> Unit)? = null) {
        suspend { boxRepository.findFirst(userHelper.getCurrentUserId()) }.execute { boxDetail ->
            copy(currentBox = boxDetail)
        }.invokeOnCompletion {
            block?.invoke()
        }
    }

    fun setCurrentBoxId(boxId: String) {
        suspend { boxRepository.findOne(boxId) }.execute { boxDetail ->
            copy(currentBox = boxDetail)
        }
    }
}