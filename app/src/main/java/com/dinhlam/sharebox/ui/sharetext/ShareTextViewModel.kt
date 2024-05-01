package com.dinhlam.sharebox.ui.sharetext

import androidx.lifecycle.SavedStateHandle
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.model.ShareData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShareTextViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val shareRepository: ShareRepository
) : BaseViewModel<ShareTextState>(ShareTextState(savedState[AppExtras.EXTRA_SHARE_ID])) {

    init {
        consume(ShareTextState::shareId) { shareId ->
            shareId?.let { id ->
                suspend {
                    shareRepository.findOne(id)
                }.execute { asyncLoad ->
                    copy(shareDetail = asyncLoad.data)
                }
            }
        }
    }

    fun save(shareId: String, newText: String?) {
        suspend {
            val share = shareRepository.findOneRaw(shareId) ?: error("No Found")
            val newShare = share.copy(shareData = ShareData.ShareText(newText.orEmpty()))
            shareRepository.update(newShare)
        }.execute { asyncLoad ->
            copy(asyncLoadSave = asyncLoad)
        }
    }
}