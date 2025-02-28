package com.dinhlam.sharebox.ui.link

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityShareLinkBinding
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.getSystemServiceCompat
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.hideKeyboard
import com.dinhlam.sharebox.extensions.isWebLink
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.listmodel.CircleFontAwesomeIconButtonListModel
import com.dinhlam.sharebox.model.Spacing
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.view.FontAwesomeIconView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShareLinkActivity :
    BaseActivity<ActivityShareLinkBinding>() {

    private val chooseBoxToGoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                val boxId = data.getStringExtra(AppExtras.EXTRA_BOX_ID)
                val boxName = data.getStringExtra(AppExtras.EXTRA_BOX_NAME)
                gotoLink(getCorrectLink(), boxId, boxName)
            }
        }

    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var router: Router

    private val adapter = BaseListAdapter.create {
        CircleFontAwesomeIconButtonListModel(
            "google",
            "f1a0",
            iconStyle = FontAwesomeIconView.IconStyle.BRANDS_REGULAR,
            onClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                setWebLink("https://google.com")
            })
        ).attachTo(this)

        CircleFontAwesomeIconButtonListModel(
            "reddit",
            "f1a1",
            iconStyle = FontAwesomeIconView.IconStyle.BRANDS_REGULAR,
            margin = Spacing.Only(start = 16.dp()),
            onClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                setWebLink("https://www.reddit.com/")
            })
        ).attachTo(this)

        CircleFontAwesomeIconButtonListModel(
            "medium",
            "f23a",
            iconStyle = FontAwesomeIconView.IconStyle.BRANDS_REGULAR,
            margin = Spacing.Only(start = 16.dp()),
            onClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                setWebLink("https://www.medium.com/")
            })
        ).attachTo(this)

        CircleFontAwesomeIconButtonListModel(
            "hacker_news",
            "f1d4",
            iconStyle = FontAwesomeIconView.IconStyle.BRANDS_REGULAR,
            margin = Spacing.Only(start = 16.dp()),
            onClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                setWebLink("https://thehackernews.com/")
            })
        ).attachTo(this)
    }

    override fun onCreateViewBinding(): ActivityShareLinkBinding {
        return ActivityShareLinkBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)

        handleUri()

        binding.buttonGo.setOnClickListener {
            onGo()
        }

        adapter.attachTo(binding.recyclerView, this)

        binding.editLink.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onGo()
            }
            true
        }

        binding.buttonPaste.setOnClickListener {
            val clipboardData = pickWebLinkFromClipboard()?.toString() ?: return@setOnClickListener
            binding.editLink.setText(clipboardData)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleUri()
    }

    private fun handleUri() {
        binding.editLink.postDelayed({
            val uri = intent.data ?: return@postDelayed
            binding.editLink.setText(uri.toString())
        }, 500)
    }

    private fun onGo() {
        binding.editLink.hideKeyboard()
        val correctLink = getCorrectLink().takeIfNotNullOrBlank()
        if (correctLink == null) {
            showToast(getString(R.string.require_input_link))
            return
        }

        if (!correctLink.isWebLink()) {
            showToast(getString(R.string.require_input_correct_weblink))
            return
        }

        chooseBoxToGoLauncher.launch(router.boxList(this, getString(R.string.choose_box_for_web)))
    }

    private fun getCorrectLink(): String {
        val link = binding.editLink.getTrimmedText().takeIfNotNullOrBlank() ?: return ""
        return if (link.startsWith("http://") || link.startsWith("https://")) {
            link
        } else {
            "https://$link"
        }
    }

    private fun gotoLink(link: String, boxId: String?, boxName: String?) {
        router.moveToChromeCustomTab(
            this,
            link,
            boxId,
            boxName
        )
    }

    private fun setWebLink(link: String) {
        binding.editLink.setText(link)
    }

    private fun pickWebLinkFromClipboard(): Uri? {
        val clipboardManager = getSystemServiceCompat<ClipboardManager>(Context.CLIPBOARD_SERVICE)
        if (!clipboardManager.hasPrimaryClip()) {
            return null
        }
        val clipItemCount = clipboardManager.primaryClip?.itemCount ?: 0
        if (clipItemCount == 0) {
            return null
        }
        val text = clipboardManager.primaryClip?.getItemAt(0)?.text?.toString()
            ?: return null

        if (text.isWebLink()) {
            return Uri.parse(text)
        }

        return null
    }
}