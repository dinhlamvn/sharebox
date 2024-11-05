package com.dinhlam.sharebox.ui.clipboard

import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.databinding.ActivityClipboardBinding
import com.dinhlam.sharebox.extensions.getSystemServiceCompat
import com.dinhlam.sharebox.extensions.isWebLink
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.ui.sharereceive.ShareReceiveActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ClipboardActivity : BaseActivity<ActivityClipboardBinding>() {

    @Inject
    lateinit var router: Router

    override fun onCreateViewBinding(): ActivityClipboardBinding {
        return ActivityClipboardBinding.inflate(layoutInflater)
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
            return returnNothingToArchive()
        }
        val clipItemCount = clipboardManager.primaryClip?.itemCount ?: 0
        if (clipItemCount == 0) {
            return
        }
        val clipItem = clipboardManager.primaryClip?.getItemAt(0) ?: return returnNothingToArchive()
        val intent = clipItem.text?.toString()?.let(::handleShareData)
            ?: clipItem.uri?.let(::handleShareData) ?: return returnNothingToArchive()
        startActivity(intent)
        finish()
    }

    private fun returnNothingToArchive() {
        showToast(R.string.nothing)
        if (isTaskRoot) {
            finishAndRemoveTask()
        } else {
            finish()
        }
    }

    private fun handleShareData(text: String): Intent {
        return if (text.isWebLink()) {
            router.shareLink(this, Uri.parse(text))
        } else {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/*"
                component = ComponentName(packageName, ShareReceiveActivity::class.java.name)
                putExtra(Intent.EXTRA_TEXT, text)
            }
        }
    }

    private fun handleShareData(uri: Uri): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            component =
                ComponentName(packageName, ShareReceiveActivity::class.java.name)
            putExtra(Intent.EXTRA_STREAM, uri)
        }
    }
}