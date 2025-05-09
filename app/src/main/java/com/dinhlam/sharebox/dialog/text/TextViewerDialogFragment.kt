package com.dinhlam.sharebox.dialog.text

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.FragmentManager
import com.dinhlam.sharebox.base.BaseDialogFragment
import com.dinhlam.sharebox.databinding.DialogFragmentTextViewerBinding

class TextViewerDialogFragment : BaseDialogFragment<DialogFragmentTextViewerBinding>() {

    companion object {
        @JvmStatic
        fun showDialog(fragmentManager: FragmentManager, text: String?) {
            TextViewerDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(Intent.EXTRA_TEXT, text)
                }
            }.show(fragmentManager, "dialog_text_viewer")
        }
    }

    override val isUseMaterialDialog: Boolean
        get() = false

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogFragmentTextViewerBinding {
        return DialogFragmentTextViewerBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val textContent = arguments?.getString(Intent.EXTRA_TEXT) ?: ""
        val htmlText = textContent.replace("\n", "<br>")
        binding.textContent.text =
            HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }
}
