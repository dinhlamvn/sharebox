package com.dinhlam.sharebox.model

import java.io.File

sealed class DownloadState(open val progress: Int) {
    data class Downloading(override val progress: Int) : DownloadState(progress)
    data class Finished(val downloadFile: File) : DownloadState(100)
    data class Failed(val error: Throwable? = null) : DownloadState(0)
}
