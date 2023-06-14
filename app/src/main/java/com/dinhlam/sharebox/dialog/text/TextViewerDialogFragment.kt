package com.dinhlam.sharebox.dialog.text

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import com.dinhlam.sharebox.base.BaseDialogFragment
import com.dinhlam.sharebox.databinding.DialogTextViewerBinding

class TextViewerDialogFragment : BaseDialogFragment<DialogTextViewerBinding>() {

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogTextViewerBinding {
        return DialogTextViewerBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val textContent = arguments?.getString(Intent.EXTRA_TEXT) ?: ""
        val htmlText = textContent.replace("\n", "<br>")
        viewBinding.textContent.text =
            HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    override fun getSpacing(): Int {
        return 16
    }
}
