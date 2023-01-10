package com.dinhlam.sharebox.dialog.guideline

import android.graphics.Bitmap
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

class GuidelineDialogFragment : BaseDialogFragment<DialogGuidelineBinding>() {

    private val webViewAssetLoader by lazy {
        WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(requireContext()))
            .addPathHandler("/res/", WebViewAssetLoader.ResourcesPathHandler(requireContext()))
            .build()
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
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
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                viewBinding.contentLoadingProgress.show()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                viewBinding.contentLoadingProgress.hide()
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                return webViewAssetLoader.shouldInterceptRequest(request!!.url)
            }
        }

        viewBinding.webView.loadUrl("https://appassets.androidplatform.net/assets/guideline/guide.html")
    }

    override fun getSpacing(): Int {
        return 32
    }
}