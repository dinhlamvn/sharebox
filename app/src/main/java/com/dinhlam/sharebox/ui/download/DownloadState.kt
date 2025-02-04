package com.dinhlam.sharebox.ui.download

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.DownloadContent

data class DownloadState(
    val asyncLoadDownload: BaseViewModel.AsyncLoad<DownloadContent> = BaseViewModel.AsyncLoad.UnInitialized,
) : BaseViewModel.BaseState {

}
