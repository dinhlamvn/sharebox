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
        val actionId: Int,
        val icon: String,
        val text: String,
    )
}
