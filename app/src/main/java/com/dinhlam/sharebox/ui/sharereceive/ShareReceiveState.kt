package com.dinhlam.sharebox.ui.sharereceive

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.local.entity.Folder
import com.dinhlam.sharebox.data.local.entity.HashTag
import com.dinhlam.sharebox.data.model.BookmarkCollectionDetail
import com.dinhlam.sharebox.data.model.Box
import com.dinhlam.sharebox.data.model.ShareData
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
    val bookmarkCollection: BookmarkCollectionDetail? = null,
    val showLoading: Boolean = false,
    val shareBox: Box = Box.CommunityBox
) : BaseViewModel.BaseState
