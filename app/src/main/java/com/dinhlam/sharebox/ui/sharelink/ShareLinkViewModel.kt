package com.dinhlam.sharebox.ui.sharelink

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.pref.AppSharePref
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShareLinkViewModel @Inject constructor(
    private val boxRepository: BoxRepository, private val appSharePref: AppSharePref
) : BaseViewModel<ShareLinkState>(ShareLinkState()) {

    init {
        getCurrentBox()
    }

    private fun getCurrentBox() {
        suspend {
            val boxId = appSharePref.getLatestActiveBoxId().takeIfNotNullOrBlank() ?: ""
            boxRepository.findOne(boxId)
        }.execute { boxDetail ->
            copy(currentBox = boxDetail)
        }
    }

    fun setCurrentBoxId(boxId: String) {
        suspend { boxRepository.findOne(boxId) }.execute { boxDetail ->
                copy(currentBox = boxDetail)
            }
    }
}