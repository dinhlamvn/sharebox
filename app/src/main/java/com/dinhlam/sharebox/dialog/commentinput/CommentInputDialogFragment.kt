package com.dinhlam.sharebox.dialog.commentinput

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import com.dinhlam.sharebox.base.BaseDialogFragment
import com.dinhlam.sharebox.databinding.DialogCommentInputBinding
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.hideKeyboard
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank

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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.imageSend.setOnClickListener {
            sendComment()
        }

        viewBinding.editComment.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendComment()
            }
            true
        }

        viewBinding.imageSend.isEnabled = false
        viewBinding.editComment.doAfterTextChanged { editable ->
            val text = editable?.toString() ?: ""
            val trimmedText = text.trim()
            viewBinding.imageSend.isEnabled = trimmedText.isNotEmpty()
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

    override fun getSpacing(): Int = 16
}