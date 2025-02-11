package com.dinhlam.sharebox.ui.downloadpopup

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.DownloadContent

data class BottomSheetDownloadState(
    val asyncLoadDownload: BaseViewModel.AsyncLoad<DownloadContent> = BaseViewModel.AsyncLoad.UnInitialized,
) : BaseViewModel.BaseState
