package com.dinhlam.sharekeeper.ui.share

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import com.dinhlam.sharekeeper.MainActivity
import com.dinhlam.sharekeeper.base.BaseActivity
import com.dinhlam.sharekeeper.databinding.ActivityShareBinding
import com.dinhlam.sharekeeper.extensions.asThe
import com.dinhlam.sharekeeper.loader.ImageLoader

class ShareReceiveActivity : BaseActivity<ActivityShareBinding>() {

    override fun onCreateViewBinding(): ActivityShareBinding {
        return ActivityShareBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when {
            intent.action == Intent.ACTION_SEND -> {
                if (intent.type?.startsWith("text/") == true) {
                    handleSendText(intent)
                } else if (intent.type?.startsWith("image/") == true) {
                    handleSendImage(intent)
                } else {
                    openHome()
                }
            }
            intent.action == Intent.ACTION_SEND_MULTIPLE && intent.type?.startsWith("image/") == true -> {
                handleSendMultipleImage(intent)
            }
            else -> openHome()
        }
    }

    private fun handleSendText(intent: Intent) {
        viewBinding.textShareContent.text = intent.getStringExtra(Intent.EXTRA_TEXT)
    }

    private fun handleSendImage(intent: Intent) {
        intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM).asThe<Parcelable, Uri>()
            ?.let { shareUri ->
                ImageLoader.load(this, shareUri, viewBinding.imageShareContent)
            }
    }

    private fun handleSendMultipleImage(intent: Intent) {

    }

    private fun openHome() {
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }
}