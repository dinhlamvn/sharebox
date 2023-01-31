package com.dinhlam.sharebox.ui.list

import android.app.Activity
import androidx.annotation.StringRes
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.database.entity.Share

data class ShareListState(
    val folderId: String? = null,
    val isRefreshing: Boolean = true,
    val shareList: List<Share> = emptyList(),
    val searchQuery: String = "",
    val folderName: String = "",
    @StringRes val toastRes: Int = 0,
    val resultCode: Int = Activity.RESULT_CANCELED
) : BaseViewModel.BaseState
