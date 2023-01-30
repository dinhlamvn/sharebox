package com.dinhlam.sharebox.dialog.guideline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.core.view.updateLayoutParams
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import com.dinhlam.sharebox.base.BaseDialogFragment
import com.dinhlam.sharebox.databinding.DialogGuidelineBinding
import com.dinhlam.sharebox.extensions.screenHeight
import java.util.*

class GuidelineDialogFragment : BaseDialogFragment<DialogGuidelineBinding>() {

    private val webViewAssetLoader by lazy {
        WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(requireContext()))
            .addPathHandler("/res/", WebViewAssetLoader.ResourcesPathHandler(requireContext()))
            .build()
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogGuidelineBinding {
        return DialogGuidelineBinding.inflate(inflater, container, false)
    }

    override fun onViewDidLoad(view: View, savedInstanceState: Bundle?) {
        viewBinding.container.updateLayoutParams {
            height = screenHeight().times(0.8f).toInt()
        }

        viewBinding.buttonClose.setOnClickListener {
            dismiss()
        }

        viewBinding.webView.settings.apply {
            allowFileAccess = true
            allowContentAccess = true
        }

        viewBinding.webView.webViewClient = object : WebViewClientCompat() {
            override fun shouldInterceptRequest(
                view: WebView?, request: WebResourceRequest?
            ): WebResourceResponse? {
                return webViewAssetLoader.shouldInterceptRequest(request!!.url)
            }
        }

        if (Locale.getDefault().language == "vi") {
            viewBinding.webView.loadUrl("https://appassets.androidplatform.net/assets/guideline/vi/guide.html")
        } else {
            viewBinding.webView.loadUrl("https://appassets.androidplatform.net/assets/guideline/en/guide.html")
        }
    }

    override fun getSpacing(): Int {
        return 32
    }
}