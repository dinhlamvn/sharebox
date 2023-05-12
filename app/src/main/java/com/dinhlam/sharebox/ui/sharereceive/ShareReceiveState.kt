package com.dinhlam.sharebox.ui.sharereceive

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.database.entity.Folder
import com.dinhlam.sharebox.database.entity.HashTag
import com.dinhlam.sharebox.database.entity.User
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareMode

data class ShareReceiveState(
    val activeUser: User? = null,
    val shareData: ShareData = ShareData.None,
    val isSaveSuccess: Boolean = false,
    val folders: List<Folder> = emptyList(),
    val selectedFolder: Folder? = null,
    val note: String? = null,
    val requestPassword: Boolean = false,
    val hashTags: List<HashTag> = emptyList(),
    val shareMode: ShareMode = ShareMode.ShareModeCommunity,
) : BaseViewModel.BaseState {

}
