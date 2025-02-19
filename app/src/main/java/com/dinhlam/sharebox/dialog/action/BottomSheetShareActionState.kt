package com.dinhlam.sharebox.dialog.action

import android.os.Parcelable
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.dialog.optionmenu.BottomSheetOptionsMenuDialogFragment
import com.dinhlam.sharebox.model.ShareDetail

data class BottomSheetShareActionState(
    val shareId: String,
    val shareDetail: ShareDetail? = null,
    val actions: List<Action> = emptyList(),
    val asyncUpdate: BaseViewModel.AsyncLoad<ShareDetail?> = BaseViewModel.AsyncLoad.UnInitialized,
): BaseViewModel.BaseState {

    data class Action(
        val actionId: ActionId,
        val icon: String,
        val text: String,
    )

    enum class ActionId {
        SHARE_TO,
        EDIT_NOTE,
        MOVE_TO_OTHER_BOX,
        COPY,
        DOWNLOAD,
        TAGS,
        VIEW_TAGS,
        COPY_BOX_ID,
        MOVE_TO_TRASH
    }
}
