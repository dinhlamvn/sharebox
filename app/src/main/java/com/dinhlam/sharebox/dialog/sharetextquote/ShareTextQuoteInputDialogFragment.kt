package com.dinhlam.sharebox.dialog.sharetextquote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.lifecycleScope
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseDialogFragment
import com.dinhlam.sharebox.databinding.DialogShareTextQuoteBinding
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.hideKeyboard
import com.dinhlam.sharebox.extensions.showKeyboard
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ShareTextQuoteInputDialogFragment : BaseDialogFragment<DialogShareTextQuoteBinding>() {

    fun interface OnShareTextQuoteCallback {
        fun onShareTextQuote(text: String)
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogShareTextQuoteBinding {
        return DialogShareTextQuoteBinding.inflate(inflater, container, false)
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

        viewBinding.editTextQuote.setHorizontallyScrolling(false)
        viewBinding.editTextQuote.maxLines = 5

        viewBinding.editTextQuote.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onDone()
            }
            true
        }

        viewBinding.editTextQuote.requestFocus()
        lifecycleScope.launch {
            delay(500)
            viewBinding.editTextQuote.showKeyboard()
        }
    }

    private fun onDone() {
        viewBinding.editTextQuote.hideKeyboard()
        val text = viewBinding.editTextQuote.getTrimmedText().takeIfNotNullOrBlank()
            ?: return showToast(R.string.require_input_text_quote)

        activity?.cast<OnShareTextQuoteCallback>()?.onShareTextQuote(text)
            ?: parentFragment.cast<OnShareTextQuoteCallback>()?.onShareTextQuote(text)
        viewBinding.editTextQuote.text?.clear()
        dismiss()
    }
}