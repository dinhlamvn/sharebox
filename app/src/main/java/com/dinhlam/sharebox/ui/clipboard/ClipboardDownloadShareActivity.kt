package com.dinhlam.sharebox.ui.clipboard

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.databinding.ActivityClipboardDownloadBinding
import com.dinhlam.sharebox.extensions.getSystemServiceCompat
import com.dinhlam.sharebox.extensions.isWebLink
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.utils.WorkerUtils

class ClipboardDownloadShareActivity : BaseActivity<ActivityClipboardDownloadBinding>() {

    override fun onCreateViewBinding(): ActivityClipboardDownloadBinding {
        return ActivityClipboardDownloadBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.root.post {
            handleShareData()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        binding.root.post {
            handleShareData()
        }
    }

    private fun handleShareData() {
        val clipboardManager = getSystemServiceCompat<ClipboardManager>(Context.CLIPBOARD_SERVICE)
        if (!clipboardManager.hasPrimaryClip()) {
            return returnNothingToDownload()
        }
        val clipItemCount = clipboardManager.primaryClip?.itemCount ?: 0
        if (clipItemCount == 0) {
            return
        }
        val text = clipboardManager.primaryClip?.getItemAt(0)?.text?.toString()
            ?: return returnNothingToDownload()
        handleShareData(text)
    }

    private fun returnNothingToDownload() {
        showToast(R.string.nothing_to_download)
        if (isTaskRoot) {
            finishAndRemoveTask()
        } else {
            finish()
        }
    }

    private fun handleShareData(text: String) {
        if (!text.isWebLink()) {
            showToast(R.string.nothing_to_download)
            if (isTaskRoot) {
                finishAndRemoveTask()
            } else {
                finish()
            }
            return
        }
        WorkerUtils.enqueueDownloadShare(this, text)
        if (isTaskRoot) {
            finishAndRemoveTask()
        } else {
            finish()
        }
    }
}