package com.dinhlam.sharebox.downloader

import com.dinhlam.sharebox.model.DownloadContent

interface Downloader {
    suspend fun download(downloadUrl: String): DownloadContent
}