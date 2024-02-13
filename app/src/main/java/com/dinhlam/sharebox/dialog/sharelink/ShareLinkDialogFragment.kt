package com.dinhlam.sharebox.dialog.sharelink

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModelDialogFragment
import com.dinhlam.sharebox.databinding.DialogShareLinkBinding
import com.dinhlam.sharebox.dialog.box.BoxSelectionDialogFragment
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.hideKeyboard
import com.dinhlam.sharebox.extensions.isWebLink
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.showKeyboard
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.utils.Icons
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ShareLinkDialogFragment :
    BaseViewModelDialogFragment<ShareLinkDialogState, ShareLinkDialogViewModel, DialogShareLinkBinding>(), BoxSelectionDialogFragment.OnBoxSelectedListener {

    fun interface OnShareLinkCallback {
        fun onShareLink(link: String, boxDetail: BoxDetail?)
    }

    @Inject
    lateinit var shareHelper: ShareHelper

    override val viewModel: ShareLinkDialogViewModel by viewModels()

    override fun onStateChanged(state: ShareLinkDialogState) {
        val boxName = state.currentBox?.boxName ?: getString(R.string.box_general)
        val isLock = state.currentBox?.passcode?.isNotBlank() ?: false
        binding.textShareBox.text = boxName
        binding.textShareBox.setDrawableCompat(
            start = Icons.boxIcon(requireContext()),
            end = if (isLock) Icons.lockIcon(requireContext()) { copy(sizeDp = 16) } else null,
        )
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogShareLinkBinding {
        return DialogShareLinkBinding.inflate(inflater, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.apply {
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonDone.setOnClickListener {
            onDone()
        }

        binding.containerShareBox.setOnClickListener {
            shareHelper.showBoxSelectionDialog(childFragmentManager)
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

        val correctLink = if (!link.startsWith("https://")) {
            "https://$link"
        } else if (!link.startsWith("http://")) {
            "http://$link"
        } else {
            link
        }

        if (!correctLink.isWebLink()) {
            return@getState showToast(R.string.require_input_correct_weblink)
        }

        parentFragment.cast<OnShareLinkCallback>()?.onShareLink(correctLink, state.currentBox)
            ?: activity.cast<OnShareLinkCallback>()
                ?.onShareLink(correctLink, state.currentBox)
        binding.editLink.text?.clear()
        dismiss()
    }

    override fun onBoxSelected(boxId: String) {
        viewModel.setCurrentBoxId(boxId)
    }
}