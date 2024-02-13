package com.dinhlam.sharebox.dialog.commentinput

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.dinhlam.sharebox.base.BaseDialogFragment
import com.dinhlam.sharebox.databinding.DialogCommentInputBinding
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.hideKeyboard
import com.dinhlam.sharebox.extensions.showKeyboard
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.extensions.trimmedString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CommentInputDialogFragment : BaseDialogFragment<DialogCommentInputBinding>() {

    fun interface OnSubmitCommentCallback {
        fun onSubmitComment(comment: String)
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogCommentInputBinding {
        return DialogCommentInputBinding.inflate(inflater, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.apply {
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonSend.setOnClickListener {
            sendComment()
        }

        binding.editComment.setHorizontallyScrolling(false)
        binding.editComment.maxLines = 5

        binding.editComment.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendComment()
            }
            true
        }

        binding.buttonSend.isEnabled = false
        binding.editComment.doAfterTextChanged { editable ->
            binding.buttonSend.isEnabled = editable.trimmedString().isNotEmpty()
        }

        binding.editComment.requestFocus()
        lifecycleScope.launch {
            delay(500)
            binding.editComment.showKeyboard()
        }
    }

    private fun sendComment() {
        binding.editComment.hideKeyboard()
        binding.editComment.getTrimmedText().takeIfNotNullOrBlank()?.let { nonBlankText ->
            parentFragment.cast<OnSubmitCommentCallback>()
                ?.onSubmitComment(nonBlankText)
        }
        binding.editComment.text?.clear()
        dismiss()
    }
}