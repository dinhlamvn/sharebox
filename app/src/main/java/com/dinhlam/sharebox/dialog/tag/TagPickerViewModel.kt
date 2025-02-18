package com.dinhlam.sharebox.dialog.tag

import androidx.lifecycle.SavedStateHandle
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.TagRepository
import com.dinhlam.sharebox.extensions.getNonNull
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TagPickerViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val tagRepository: TagRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<TagPickerState>(TagPickerState(savedStateHandle.getNonNull(AppExtras.EXTRA_SHARE_ID))) {

    init {
        onChange(TagPickerState::shareId, ::loadShareSelectedTag)
        getTags()
    }

    private fun loadShareSelectedTag(shareId: String) {
        suspend {
            val share = shareRepository.findOneRaw(shareId)
            share?.tagId
        }.execute { asyncLoad ->
            copy(tagIdPicked = asyncLoad.data)
        }
    }

    private fun getTags() = suspend {
        tagRepository.readAll()
    }.execute { asyncLoad ->
        copy(tags = asyncLoad.data.orEmpty())
    }

    fun setSelectedTag(tagId: Int) {
        setState {
            if (tagId == tagIdPicked) {
                copy(tagIdPicked = null)
            } else {
                copy(tagIdPicked = tagId)
            }
        }
    }

    fun saveShareTag() = getState { state ->
        suspend {
            val share = shareRepository.findOneRaw(state.shareId)!!
            shareRepository.update(share.copy(tagId = state.tagIdPicked))!!
        }.execute { asyncLoad ->
            copy(asyncLoadSaveTag = asyncLoad)
        }
    }
}