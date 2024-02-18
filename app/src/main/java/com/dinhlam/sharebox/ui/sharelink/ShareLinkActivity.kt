package com.dinhlam.sharebox.ui.sharelink

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityShareLinkBinding
import com.dinhlam.sharebox.dialog.box.BoxSelectionDialogFragment
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.getDrawableCompat
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.hideKeyboard
import com.dinhlam.sharebox.extensions.isWebLink
import com.dinhlam.sharebox.extensions.setDrawableCompat
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
    BaseViewModelActivity<ShareLinkState, ShareLinkViewModel, ActivityShareLinkBinding>(),
    BoxSelectionDialogFragment.OnBoxSelectedListener {

    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var router: Router

    private val adapter = BaseListAdapter.createAdapter {
        CircleIconListModel(
            "google", Icons.googleIcon(this@ShareLinkActivity), size = 32.dp(),
            onClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                gotoLink("https://google.com")
            })
        ).attachTo(this)

        CircleIconListModel(
            "tiktok",
            getDrawableCompat(R.drawable.ic_tiktok),
            size = 32.dp(),
            margin = Spacing.Only(start = 16.dp()),
            onClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                gotoLink("https://tiktok.com")
            })
        ).attachTo(this)

        CircleIconListModel(
            "youtube",
            Icons.youtubeIcon(this@ShareLinkActivity),
            size = 32.dp(),
            margin = Spacing.Only(start = 16.dp()),
            onClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                gotoLink("https://youtube.com")
            })
        ).attachTo(this)

        CircleIconListModel(
            "cand",
            getDrawableCompat(R.drawable.ic_cand),
            size = 32.dp(),
            margin = Spacing.Only(start = 16.dp()),
            onClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                gotoLink("https://cand.com.vn")
            })
        ).attachTo(this)

        CircleIconListModel(
            "thanh_nien",
            getDrawableCompat(R.drawable.ic_thanh_nien),
            size = 32.dp(),
            margin = Spacing.Only(start = 16.dp()),
            onClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                gotoLink("https://thanhnien.vn")
            })
        ).attachTo(this)

        CircleIconListModel(
            "zing_news",
            getDrawableCompat(R.drawable.ic_zing_news),
            size = 32.dp(),
            margin = Spacing.Only(start = 16.dp()),
            onClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                gotoLink("https://zingnews.vn")
            })
        ).attachTo(this)

        CircleIconListModel(
            "tuoi_tre",
            getDrawableCompat(R.drawable.ic_tuoi_tre),
            size = 32.dp(),
            margin = Spacing.Only(start = 16.dp()),
            onClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                gotoLink("https://tuoitre.vn")
            })
        ).attachTo(this)
    }

    private val createBoxResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getStringExtra(AppExtras.EXTRA_BOX_ID)?.let(viewModel::setCurrentBoxId)
            }
        }

    override val viewModel: ShareLinkViewModel by viewModels()

    override fun onStateChanged(state: ShareLinkState) {
        val boxName = state.currentBox?.boxName ?: getString(R.string.box_general)
        val isLock = state.currentBox?.passcode?.isNotBlank() ?: false
        binding.textShareBox.text = boxName
        binding.textShareBox.setDrawableCompat(
            start = Icons.boxIcon(this),
            end = if (isLock) Icons.lockIcon(this) { copy(sizeDp = 16) } else null,
        )
    }

    override fun onCreateViewBinding(): ActivityShareLinkBinding {
        return ActivityShareLinkBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.buttonDone.setOnClickListener {
            onDone()
        }

        adapter.attachTo(binding.recyclerView, this)

        binding.imageAddBox.setImageDrawable(Icons.addIcon(this))
        binding.imageAddBox.setOnClickListener {
            createBoxResultLauncher.launch(router.boxIntent(this))
        }

        binding.containerShareBox.setOnClickListener {
            shareHelper.showBoxSelectionDialog(supportFragmentManager)
        }

        binding.editLink.setHorizontallyScrolling(false)
        binding.editLink.maxLines = Int.MAX_VALUE

        binding.editLink.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onDone()
            }
            true
        }
    }

    private fun onDone() {
        binding.editLink.hideKeyboard()
        val link =
            binding.editLink.getTrimmedText().takeIfNotNullOrBlank() ?: return showToast(
                R.string.require_input_link
            )

        val correctLink = if (link.startsWith("http://") || link.startsWith("https://")) {
            link
        } else {
            "https://$link"
        }

        if (!correctLink.isWebLink()) {
            return showToast(R.string.require_input_correct_weblink)
        }

        gotoLink(correctLink)
    }

    private fun gotoLink(link: String) = getState(viewModel) { state ->
        router.moveToChromeCustomTab(
            this,
            link,
            state.currentBox?.boxId,
            state.currentBox?.boxName,
            shareHelper.isSupportDownloadLink(link)
        )
    }

    override fun onBoxSelected(boxId: String) {
        viewModel.setCurrentBoxId(boxId)
    }
}