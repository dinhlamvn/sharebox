package com.dinhlam.sharebox.receiver

import android.content.Context
import android.content.Intent
import com.dinhlam.sharebox.utils.WorkerUtils

class CustomTabsDownloadBroadcastReceiver : BaseBroadcastReceiver() {

    companion object {
        const val REQUEST_CODE = 1589
    }

    override fun onReceive(context: Context?, intent: Intent) {
        val url = intent.dataString ?: return
        context?.let { ctx ->
            WorkerUtils.enqueueDownloadShare(ctx, url)
        }
    }

}