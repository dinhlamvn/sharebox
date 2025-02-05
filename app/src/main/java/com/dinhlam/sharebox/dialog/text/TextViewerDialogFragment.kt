package com.dinhlam.sharebox.dialog.text

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import com.dinhlam.sharebox.base.BaseDialogFragment
import com.dinhlam.sharebox.databinding.DialogTextViewerBinding
import com.dinhlam.sharebox.extensions.heightPercentage
import com.dinhlam.sharebox.extensions.updateHeight

class TextViewerDialogFragment : BaseDialogFragment<DialogTextViewerBinding>() {

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogTextViewerBinding {
        return DialogTextViewerBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.scrollView.updateHeight(heightPercentage(70))

        val textContent = arguments?.getString(Intent.EXTRA_TEXT) ?: ""
        val htmlText = textContent.replace("\n", "<br>")
        binding.textContent.text =
            HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }
}
