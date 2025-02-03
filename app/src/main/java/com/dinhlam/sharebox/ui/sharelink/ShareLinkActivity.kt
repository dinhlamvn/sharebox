package com.dinhlam.sharebox.ui.sharelink

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.UiThread
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityShareLinkBinding
import com.dinhlam.sharebox.extensions.doAfterTextChangedDebounce
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.getDrawableCompat
import com.dinhlam.sharebox.extensions.getSystemServiceCompat
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.hideKeyboard
import com.dinhlam.sharebox.extensions.isWebLink
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.listmodel.CircleIconListModel
import com.dinhlam.sharebox.model.Spacing
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.Icons
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShareLinkActivity :
    BaseViewModelActivity<ShareLinkState, ShareLinkViewModel, ActivityShareLinkBinding>() {

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
        CircleIconListModel(
            "google",
            Icons.googleIcon(this@ShareLinkActivity),
            size = 32.dp(),
            onClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                setWebLink("https://google.com")
            })
        ).attachTo(this)

        CircleIconListModel(
            "tiktok",
            getDrawableCompat(R.drawable.ic_tiktok),
            size = 32.dp(),
            margin = Spacing.Only(start = 16.dp()),
            onClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                setWebLink("https://tiktok.com")
            })
        ).attachTo(this)

        CircleIconListModel(
            "youtube",
            Icons.youtubeIcon(this@ShareLinkActivity),
            size = 32.dp(),
            margin = Spacing.Only(start = 16.dp()),
            onClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                setWebLink("https://youtube.com")
            })
        ).attachTo(this)

        CircleIconListModel(
            "cand",
            getDrawableCompat(R.drawable.ic_cand),
            size = 32.dp(),
            margin = Spacing.Only(start = 16.dp()),
            onClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                setWebLink("https://cand.com.vn")
            })
        ).attachTo(this)

        CircleIconListModel(
            "thanh_nien",
            getDrawableCompat(R.drawable.ic_thanh_nien),
            size = 32.dp(),
            margin = Spacing.Only(start = 16.dp()),
            onClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                setWebLink("https://thanhnien.vn")
            })
        ).attachTo(this)

        CircleIconListModel(
            "zing_news",
            getDrawableCompat(R.drawable.ic_zing_news),
            size = 32.dp(),
            margin = Spacing.Only(start = 16.dp()),
            onClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                setWebLink("https://zingnews.vn")
            })
        ).attachTo(this)

        CircleIconListModel(
            "tuoi_tre",
            getDrawableCompat(R.drawable.ic_tuoi_tre),
            size = 32.dp(),
            margin = Spacing.Only(start = 16.dp()),
            onClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                setWebLink("https://tuoitre.vn")
            })
        ).attachTo(this)
    }

    override val viewModel: ShareLinkViewModel by viewModels()

    override fun onStateChanged(state: ShareLinkState) {
        showLinkError(state.linkError)
        adapter.requestBuildListModels()
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

        binding.editLink.doAfterTextChangedDebounce(scope = lifecycleScope) {
            viewModel.setLinkError(null)
        }

        binding.buttonPaste.setOnClickListener {
            val clipboardData = pickWebLinkFromClipboard()?.toString() ?: return@setOnClickListener
            binding.editLink.setText(clipboardData)
        }

        onChange(ShareLinkState::asyncLoadArchive) { asyncLoad ->
            if (asyncLoad.success) {
                showToast(R.string.shares_success)
                finish()
            }
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
        val correctLink = getCorrectLink().takeIfNotNullOrBlank() ?: return viewModel.setLinkError(
            getString(R.string.require_input_link)
        )

        if (!correctLink.isWebLink()) {
            return viewModel.setLinkError(getString(R.string.require_input_correct_weblink))
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
            boxName,
            shareHelper.isSupportDownloadLink(link)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @UiThread
    private fun showLinkError(error: String?) {
        binding.textError.error = error
        binding.textError.text = error
        binding.textError.isVisible = error.isNullOrBlank().not()
    }
}