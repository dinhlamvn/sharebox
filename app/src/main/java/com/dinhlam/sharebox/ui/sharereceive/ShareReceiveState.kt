package com.dinhlam.sharebox.ui.sharereceive

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.local.entity.Folder
import com.dinhlam.sharebox.data.local.entity.HashTag
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.data.model.ShareMode
import com.dinhlam.sharebox.data.model.UserDetail

data class ShareReceiveState(
    val activeUser: UserDetail? = null,
    val shareData: ShareData? = null,
    val isSaveSuccess: Boolean = false,
    val folders: List<Folder> = emptyList(),
    val selectedFolder: Folder? = null,
    val note: String? = null,
    val requestPassword: Boolean = false,
    val hashTags: List<HashTag> = emptyList(),
    val shareMode: ShareMode = ShareMode.ShareModeCommunity,
) : BaseViewModel.BaseState {

}
