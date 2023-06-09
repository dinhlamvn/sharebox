package com.dinhlam.sharebox.ui.home.videomixer.modelview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.core.view.isVisible
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.data.model.ShareDetail
import com.dinhlam.sharebox.databinding.ModelViewVideoYoutubeBinding
import com.dinhlam.sharebox.extensions.asBookmarkIconLight
import com.dinhlam.sharebox.extensions.asLikeIconLight
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.utils.IconUtils

data class YoutubeVideoModelView(
    val id: String,
    val videoId: String,
    val shareDetail: ShareDetail,
    val actionViewInSource: BaseListAdapter.NoHashProp<Function1<ShareData, Unit>> = BaseListAdapter.NoHashProp(
        null
    ),
    val actionShareToOther: BaseListAdapter.NoHashProp<Function1<String, Unit>> = BaseListAdapter.NoHashProp(
        null
    ),
    val actionLike: BaseListAdapter.NoHashProp<Function1<String, Unit>> = BaseListAdapter.NoHashProp(
        null
    ),
    val actionComment: BaseListAdapter.NoHashProp<Function1<String, Unit>> = BaseListAdapter.NoHashProp(
        null
    ),
    val actionBookmark: BaseListAdapter.NoHashProp<Function1<String, Unit>> = BaseListAdapter.NoHashProp(
        null
    ),
) : BaseListAdapter.BaseModelView(id) {
    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return YoutubeVideoViewHolder(
            ModelViewVideoYoutubeBinding.inflate(
                inflater, container, false
            )
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    private class YoutubeVideoViewHolder(binding: ModelViewVideoYoutubeBinding) :
        BaseListAdapter.BaseViewHolder<YoutubeVideoModelView, ModelViewVideoYoutubeBinding>(binding) {

        private val mainHandler = Handler(Looper.getMainLooper())

        private val webViewAssetLoader by lazy {
            WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(buildContext))
                .addPathHandler("/res/", WebViewAssetLoader.ResourcesPathHandler(buildContext))
                .build()
        }

        class YoutubeJsInterface(
            private val handler: Handler, private val viewBinding: ModelViewVideoYoutubeBinding
        ) {

            @JavascriptInterface
            fun onVideoReady() {
                handler.post {
                    viewBinding.contentLoadingProgress.hide()
                }
            }
        }

        init {
            binding.textViewInSource.setDrawableCompat(end = IconUtils.openIcon(buildContext) {
                copy(sizeDp = 16, colorRes = android.R.color.white)
            })
            binding.bottomAction.apply {
                setCommentIcon(IconUtils.commentIconLight(buildContext))
                setShareIcon(IconUtils.shareIconLight(buildContext))
                setLikeTextColor(Color.WHITE)
                setCommentTextColor(Color.WHITE)
            }
            binding.webView.setBackgroundColor(Color.BLACK)
            binding.webView.addJavascriptInterface(
                YoutubeJsInterface(mainHandler, binding), "android"
            )
            binding.webView.settings.apply {
                javaScriptEnabled = true
            }
            binding.webView.webViewClient = object : WebViewClientCompat() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    binding.contentLoadingProgress.show()
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.contentLoadingProgress.hide()
                }

                override fun shouldInterceptRequest(
                    view: WebView?, request: WebResourceRequest?
                ): WebResourceResponse? {
                    return webViewAssetLoader.shouldInterceptRequest(request!!.url)
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView, request: WebResourceRequest
                ): Boolean {
                    return true
                }
            }
        }

        override fun onBind(model: YoutubeVideoModelView, position: Int) {
            val html = buildContext.assets.open("youtube_embed/embed.html").reader().readText()
            val formatHtml = String.format(html, model.videoId)
            val encodeHtml = Base64.encodeToString(formatHtml.toByteArray(), Base64.NO_PADDING)
            binding.webView.loadData(encodeHtml, "text/html", "base64")

            binding.bottomAction.setBookmarkIcon(
                model.shareDetail.bookmarked.asBookmarkIconLight(buildContext)
            )

            binding.textViewInSource.setOnClickListener {
                model.actionViewInSource.prop?.invoke(model.shareDetail.shareData)
            }

            binding.bottomAction.setLikeIcon(model.shareDetail.liked.asLikeIconLight(buildContext))

            binding.bottomAction.setOnShareClickListener {
                model.actionShareToOther.prop?.invoke(model.shareDetail.shareId)
            }

            binding.bottomAction.setOnCommentClickListener {
                model.actionComment.prop?.invoke(model.shareDetail.shareId)
            }

            binding.bottomAction.setOnLikeClickListener {
                model.actionLike.prop?.invoke(model.shareDetail.shareId)
            }

            binding.bottomAction.setOnBookmarkClickListener {
                model.actionBookmark.prop?.invoke(model.shareDetail.shareId)
            }

            binding.bottomAction.setLikeNumber(model.shareDetail.likeNumber)
            binding.bottomAction.setCommentNumber(model.shareDetail.commentNumber)

            binding.textViewName.text = model.shareDetail.user.name
            ImageLoader.INSTANCE.load(
                buildContext, model.shareDetail.user.avatar, binding.imageAvatar
            ) {
                copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
            }

            model.shareDetail.shareNote.takeIfNotNullOrBlank()?.let { text ->
                binding.textNote.isVisible = true
                binding.textNote.setReadMoreText(text)
            } ?: binding.textNote.apply {
                text = null
                isVisible = false
            }
        }

        override fun onUnBind() {
            binding.webView.loadUrl("auto:blank")
            binding.textNote.text = null
            binding.bottomAction.release()
            ImageLoader.INSTANCE.release(buildContext, binding.imageAvatar)
        }
    }
}
