package com.dinhlam.sharebox.dialog.download

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.DownloadState

data class DownloadFileState(
    val downloadState: DownloadState = DownloadState.Downloading(0, 0, 0),
) : BaseViewModel.BaseState
