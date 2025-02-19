package com.dinhlam.sharebox.ui.tags

import androidx.lifecycle.SavedStateHandle
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.local.entity.Tag
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.TagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TagsViewModel @Inject constructor(
    private val tagRepository: TagRepository,
    private val shareRepository: ShareRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<TagsState>(TagsState()) {

    init {
        onChange(TagsState::tagActive) { tag ->
            getShares(tag)
        }
        getTags(savedStateHandle[AppExtras.EXTRA_ID])
    }

    private fun getShares(tag: Tag?) {
        val tagActive = tag ?: return setState { copy(shares = emptyList()) }
        suspend {
            shareRepository.findAll(tagActive.id)
        }.execute { asyncLoad ->
            copy(asyncLoadShare = asyncLoad, shares = asyncLoad.data.orEmpty())
        }
    }

    private fun getTags(tagId: Int?) {
        suspend {
            val tags = tagRepository.readAll()
            val tagActive = if (tagId != null) {
                tagRepository.readOne(tagId)
            } else {
                tags.first()
            }
            tagActive to tags
        }.execute { asyncLoad ->
            copy(
                tags = asyncLoad.data?.second ?: tags,
                tagActive = asyncLoad.data?.first ?: tagActive
            )
        }
    }

    fun refresh() = getState { state ->
        state.tagActive?.let(::getShares)
    }

    fun setActiveTag(tag: Tag) {
        setState { copy(tagActive = tag) }
    }

    fun clearTag(shareId: String) = doInBackground {
        val share = shareRepository.findOneRaw(shareId)!!
        val updated = shareRepository.update(share.copy(tagId = null))
        if (updated != null) {
            setState { copy(shares = shares.filterNot { share -> share.shareId == shareId }) }
        }
    }
}