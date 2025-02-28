package com.dinhlam.sharebox.dialog.action

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.extensions.getNonNull
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BottomSheetShareActionViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
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

    fun buildActions(context: Context, share: ShareDetail?) {
        val shareDetail = share ?: return setState { copy(actions = emptyList()) }
        val shareData = shareDetail.shareData
        val actions = buildList {
            add(
                BottomSheetShareActionState.Action(
                    BottomSheetShareActionState.ActionId.SHARE_TO,
                    "f064",
                    context.getString(R.string.share_to)
                )
            )
            add(
                BottomSheetShareActionState.Action(
                    BottomSheetShareActionState.ActionId.EDIT_NOTE,
                    "f044",
                    context.getString(R.string.edit_note)
                )
            )
            add(
                BottomSheetShareActionState.Action(
                    BottomSheetShareActionState.ActionId.MOVE_TO_OTHER_BOX,
                    "f061",
                    context.getString(R.string.move_to)
                )
            )

            if (shareData is ShareData.ShareText || shareData is ShareData.ShareUrl || shareData is ShareData.ShareCheckList) {
                add(
                    BottomSheetShareActionState.Action(
                        BottomSheetShareActionState.ActionId.COPY,
                        "f0c5",
                        context.getString(R.string.copy)
                    )
                )
            }

            if (shareData is ShareData.ShareUrl || shareData is ShareData.ShareFile) {
                add(
                    BottomSheetShareActionState.Action(
                        BottomSheetShareActionState.ActionId.DOWNLOAD,
                        "f56d",
                        context.getString(R.string.download)
                    )
                )
            }

            if (shareData is ShareData.ShareCheckList) {
                add(
                    BottomSheetShareActionState.Action(
                        BottomSheetShareActionState.ActionId.EDIT_CHECK_LIST,
                        "f0ae",
                        context.getString(R.string.edit_checklist)
                    )
                )
            }

            add(
                BottomSheetShareActionState.Action(
                    BottomSheetShareActionState.ActionId.TAGS,
                    "f02b",
                    context.getString(R.string.tags)
                )
            )
            if (shareDetail.tagId != null) {
                add(
                    BottomSheetShareActionState.Action(
                        BottomSheetShareActionState.ActionId.VIEW_TAGS,
                        "f03a",
                        context.getString(R.string.view_tags)
                    )
                )
            }

            add(
                BottomSheetShareActionState.Action(
                    BottomSheetShareActionState.ActionId.COPY_BOX_ID,
                    "f0c5",
                    context.getString(R.string.copy_box_id)
                )
            )
            add(
                BottomSheetShareActionState.Action(
                    BottomSheetShareActionState.ActionId.MOVE_TO_TRASH,
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

    fun moveShareToTrash(shareId: String) {
        suspend {
            val share = shareRepository.findOneRaw(shareId)!!
            shareRepository.update(share.copy(shareBoxId = null))
            shareRepository.findOne(shareId)!!
        }.execute { asyncLoad -> copy(asyncUpdate = asyncLoad) }
    }
}