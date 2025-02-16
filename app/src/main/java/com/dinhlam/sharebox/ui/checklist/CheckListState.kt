package com.dinhlam.sharebox.ui.checklist

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareDetail

data class CheckListState(
    val shareDetail: ShareDetail?,
    val currentBox: BoxDetail? = shareDetail?.boxDetail,
    val checkListDataList: List<ShareData.ShareCheckList.CheckListData> = shareDetail?.shareData?.cast<ShareData.ShareCheckList>()?.checkListDataList.orEmpty(),
    val asyncArchive: BaseViewModel.AsyncLoad<Share> = BaseViewModel.AsyncLoad.UnInitialized,
) : BaseViewModel.BaseState