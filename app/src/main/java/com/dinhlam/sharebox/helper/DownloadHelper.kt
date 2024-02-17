package com.dinhlam.sharebox.helper

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.dinhlam.sharebox.data.network.SSSTikServices
import com.dinhlam.sharebox.extensions.getSystemServiceCompat
import com.dinhlam.sharebox.utils.UserAgentUtils
import com.dinhlam.sharebox.utils.WorkerUtils
import javax.inject.Inject

class DownloadHelper @Inject constructor(private val tikServices: SSSTikServices) {

    fun enqueueDownload(context: Context, downloadUrl: String, fileName: String) {
        val downloadManager =
            context.getSystemServiceCompat<DownloadManager>(Context.DOWNLOAD_SERVICE)
        val uri = Uri.parse(downloadUrl)
        val request = DownloadManager.Request(uri)
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
            .addRequestHeader("User-Agent", UserAgentUtils.pickRandomUserAgent())
            .setAllowedOverRoaming(false)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(fileName).setDescription("File is downloading...please wait!!")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        downloadManager.enqueue(request)
    }

    fun downloadImages(context: Context, id: String, urls: List<String>) {
        WorkerUtils.enqueueDownloadImages(context, id, urls)
    }
}