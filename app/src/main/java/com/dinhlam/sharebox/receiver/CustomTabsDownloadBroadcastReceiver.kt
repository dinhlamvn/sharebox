package com.dinhlam.sharebox.receiver

import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.utils.WorkerUtils

class CustomTabsDownloadBroadcastReceiver : BaseBroadcastReceiver() {

    companion object {
        const val REQUEST_CODE = 1589
    }

    override fun onReceive(context: Context?, intent: Intent) {
        val url = intent.dataString ?: return
        val remoteViewId = intent.getIntExtra(CustomTabsIntent.EXTRA_REMOTEVIEWS_CLICKED_ID, -1)
        if (remoteViewId == R.id.image_download) {
            context?.let { ctx ->
                WorkerUtils.enqueueDownloadShare(ctx, url)
            }
        }
    }

}