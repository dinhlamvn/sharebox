package com.dinhlam.sharebox.dialog.commentinput

import android.content.DialogInterface
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
        viewBinding.buttonSend.setOnClickListener {
            sendComment()
        }

        viewBinding.editComment.setHorizontallyScrolling(false)
        viewBinding.editComment.maxLines = 5

        viewBinding.editComment.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendComment()
            }
            true
        }

        viewBinding.buttonSend.isEnabled = false
        viewBinding.editComment.doAfterTextChanged { editable ->
            viewBinding.buttonSend.isEnabled = editable.trimmedString().isNotEmpty()
        }

        viewBinding.editComment.requestFocus()
        lifecycleScope.launch {
            delay(500)
            viewBinding.editComment.showKeyboard()
        }
    }

    private fun sendComment() {
        viewBinding.editComment.hideKeyboard()
        viewBinding.editComment.getTrimmedText().takeIfNotNullOrBlank()?.let { nonBlankText ->
            parentFragment.cast<OnSubmitCommentCallback>()
                ?.onSubmitComment(nonBlankText)
        }
        viewBinding.editComment.text?.clear()
        dismiss()
    }
}