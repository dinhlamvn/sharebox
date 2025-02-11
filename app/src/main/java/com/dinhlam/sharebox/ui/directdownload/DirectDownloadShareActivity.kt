package com.dinhlam.sharebox.ui.directdownload

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.extensions.isWebLink
import com.dinhlam.sharebox.extensions.showToast

class DirectDownloadShareActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleShareData()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShareData()
    }

    private fun handleShareData() {
        val (action, type) = intent.action to intent.type
        when {
            action == Intent.ACTION_SEND && type?.startsWith("text/") == true -> {
                val shareContent = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
                handleShareData(shareContent)
            }

            else -> {
                showToast(R.string.no_support_download_this_content)
                finishAndRemoveTask()
            }
        }
    }

    private fun handleShareData(text: String) {
        if (!text.isWebLink()) {
            showToast(R.string.no_support_download_this_content)
            finishAndRemoveTask()
            return
        }
        finishAndRemoveTask()
    }
}