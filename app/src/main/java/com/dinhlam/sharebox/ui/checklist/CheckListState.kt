package com.dinhlam.sharebox.ui.checklist

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareDetail

data class CheckListState(
    val shareId: String?,
    val shareDetail: ShareDetail? = null,
    val currentBox: BoxDetail? = null,
    val checkListDataList: List<ShareData.ShareCheckList.CheckListData> = emptyList(),
    val asyncArchive: BaseViewModel.AsyncLoad<Share> = BaseViewModel.AsyncLoad.UnInitialized,
) : BaseViewModel.BaseState