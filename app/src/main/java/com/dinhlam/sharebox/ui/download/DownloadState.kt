package com.dinhlam.sharebox.ui.download

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.DownloadData

data class DownloadState(
    val asyncLoadDownload: BaseViewModel.AsyncLoad<DownloadContent> = BaseViewModel.AsyncLoad.UnInitialized,
) : BaseViewModel.BaseState {

    data class DownloadContent(
        val videos: List<DownloadData> = emptyList(),
        val audios: List<DownloadData> = emptyList(),
        val images: List<DownloadData> = emptyList(),
    )
}
