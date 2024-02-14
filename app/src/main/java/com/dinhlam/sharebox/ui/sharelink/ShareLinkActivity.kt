package com.dinhlam.sharebox.ui.sharelink

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityShareLinkBinding
import com.dinhlam.sharebox.dialog.box.BoxSelectionDialogFragment
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.hideKeyboard
import com.dinhlam.sharebox.extensions.isWebLink
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.showKeyboard
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.Icons
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ShareLinkActivity :
    BaseViewModelActivity<ShareLinkState, ShareLinkViewModel, ActivityShareLinkBinding>(),
    BoxSelectionDialogFragment.OnBoxSelectedListener {

    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var router: Router

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

        binding.editLink.requestFocus()
        lifecycleScope.launch {
            delay(500)
            binding.editLink.showKeyboard()
        }
    }

    private fun onDone() = getState(viewModel) { state ->
        binding.editLink.hideKeyboard()
        val link =
            binding.editLink.getTrimmedText().takeIfNotNullOrBlank() ?: return@getState showToast(
                R.string.require_input_link
            )

        if (!link.isWebLink()) {
            return@getState showToast(R.string.require_input_correct_weblink)
        }

        setResult(
            Activity.RESULT_OK, Intent()
                .putExtra(AppExtras.EXTRA_URL, link)
                .putExtra(AppExtras.EXTRA_BOX_ID, state.currentBox?.boxId)
                .putExtra(AppExtras.EXTRA_BOX_NAME, state.currentBox?.boxName)
        )
        finish()
    }

    override fun onBoxSelected(boxId: String) {
        viewModel.setCurrentBoxId(boxId)
    }
}