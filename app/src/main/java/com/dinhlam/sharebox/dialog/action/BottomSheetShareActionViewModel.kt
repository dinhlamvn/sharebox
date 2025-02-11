package com.dinhlam.sharebox.dialog.action

import android.content.Context
import androidx.annotation.UiThread
import androidx.lifecycle.SavedStateHandle
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.repository.BookmarkRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.extensions.getNonNull
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BottomSheetShareActionViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val bookmarkRepository: BookmarkRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<BottomSheetShareActionState>(
    BottomSheetShareActionState(
        savedStateHandle.getNonNull(
            AppExtras.EXTRA_SHARE_ID
        )
    )
) {

    init {
        getState { state ->
            getShareDetail(state.shareId)
        }
    }

    fun buildActions(context: Context, shareDetail: ShareDetail?) {
        val shareData = shareDetail ?: return setState { copy(actions = emptyList()) }
        val actions = buildList {
            add(BottomSheetShareActionState.Action(0, "f064", context.getString(R.string.share_to)))
            add(
                BottomSheetShareActionState.Action(
                    1,
                    "f044",
                    context.getString(R.string.edit_note)
                )
            )
            add(BottomSheetShareActionState.Action(2, "f061", context.getString(R.string.move_to)))

            if (shareData.shareData is ShareData.ShareText || shareData.shareData is ShareData.ShareUrl) {
                add(BottomSheetShareActionState.Action(3, "f0c5", context.getString(R.string.copy)))
            } else {
                add(
                    BottomSheetShareActionState.Action(
                        4,
                        "f56d",
                        context.getString(R.string.download)
                    )
                )
            }
            add(BottomSheetShareActionState.Action(5, "f02e", context.getString(R.string.bookmark)))
            add(
                BottomSheetShareActionState.Action(
                    6,
                    "f0c5",
                    context.getString(R.string.copy_box_id)
                )
            )
            add(
                BottomSheetShareActionState.Action(
                    7,
                    "f1f8",
                    context.getString(R.string.move_to_trash)
                )
            )
        }

        setState { copy(actions = actions) }
    }

    private fun getShareDetail(shareId: String) {
        suspend { shareRepository.findOne(shareId) }
            .execute { asyncLoad ->
                copy(shareDetail = asyncLoad.data)
            }
    }

    fun saveShareNote(text: String?) = getState { state ->
        suspend {
            val share = shareRepository.findOneRaw(state.shareId)
            share?.let { updateShare ->
                shareRepository.update(updateShare.copy(shareNote = text))
                shareRepository.findOne(state.shareId)
            } ?: state.shareDetail
        }.execute { asyncLoad -> copy(asyncUpdate = asyncLoad) }
    }

    fun moveShareToBox(boxId: String) = getState { state ->
        suspend {
            val share = shareRepository.findOneRaw(state.shareId)
            share?.let { updateShare ->
                shareRepository.update(updateShare.copy(shareBoxId = boxId))
                shareRepository.findOne(state.shareId)
            } ?: state.shareDetail
        }.execute { asyncLoad -> copy(asyncUpdate = asyncLoad) }
    }

    fun showBookmarkCollectionPicker(shareId: String, @UiThread block: (String?) -> Unit) =
        doInBackground {
            val bookmarkDetail = bookmarkRepository.findOne(shareId)
            withContext(Dispatchers.Main) {
                block(bookmarkDetail?.bookmarkCollectionId)
            }
        }

    fun moveShareToTrash(shareId: String) {
        suspend {
            val share = shareRepository.findOneRaw(shareId)!!
            shareRepository.update(share.copy(shareBoxId = null))
            shareRepository.findOne(shareId)!!
        }.execute { asyncLoad -> copy(asyncUpdate = asyncLoad) }
    }
}