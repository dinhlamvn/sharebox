package com.dinhlam.sharebox.dialog.sharelink

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.lifecycleScope
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseDialogFragment
import com.dinhlam.sharebox.databinding.DialogShareLinkBinding
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.hideKeyboard
import com.dinhlam.sharebox.extensions.isWebLink
import com.dinhlam.sharebox.extensions.showKeyboard
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ShareLinkInputDialogFragment : BaseDialogFragment<DialogShareLinkBinding>() {

    fun interface OnShareLinkCallback {
        fun onShareLink(link: String)
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
        viewBinding.buttonDone.setOnClickListener {
            onDone()
        }

        viewBinding.editLink.setHorizontallyScrolling(false)
        viewBinding.editLink.maxLines = 5

        viewBinding.editLink.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onDone()
            }
            true
        }

        viewBinding.editLink.requestFocus()
        lifecycleScope.launch {
            delay(500)
            viewBinding.editLink.showKeyboard()
        }
    }

    private fun onDone() {
        viewBinding.editLink.hideKeyboard()
        val link = viewBinding.editLink.getTrimmedText().takeIfNotNullOrBlank() ?: return showToast(
            R.string.require_input_link
        )

        if (!link.startsWith("https://") && !link.startsWith("http://")) {
            return showToast(R.string.invalidate_link)
        }

        if (!link.isWebLink()) {
            return showToast(R.string.require_input_correct_weblink)
        }

        activity?.cast<OnShareLinkCallback>()?.onShareLink(link)
            ?: parentFragment.cast<OnShareLinkCallback>()?.onShareLink(link)
        viewBinding.editLink.text?.clear()
        dismiss()
    }
}