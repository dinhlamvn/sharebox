package com.dinhlam.sharebox.modelview.list.video

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.data.model.ShareDetail
import com.dinhlam.sharebox.databinding.ModelViewListVideoYoutubeBinding
import com.dinhlam.sharebox.extensions.asBookmarkIcon
import com.dinhlam.sharebox.extensions.asElapsedTimeDisplay
import com.dinhlam.sharebox.extensions.asLikeIcon
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.utils.IconUtils
import com.dinhlam.sharebox.utils.UserUtils

data class ListYoutubeVideoModelView(
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
        return ListYoutubeVideoViewHolder(
            ModelViewListVideoYoutubeBinding.inflate(
                inflater, container, false
            )
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    private class ListYoutubeVideoViewHolder(binding: ModelViewListVideoYoutubeBinding) :
        BaseListAdapter.BaseViewHolder<ListYoutubeVideoModelView, ModelViewListVideoYoutubeBinding>(
            binding
        ) {

        private val mainHandler = Handler(Looper.getMainLooper())

        private val webViewAssetLoader by lazy {
            WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(buildContext))
                .addPathHandler("/res/", WebViewAssetLoader.ResourcesPathHandler(buildContext))
                .build()
        }

        class YoutubeJsInterface(
            private val handler: Handler, private val viewBinding: ModelViewListVideoYoutubeBinding
        ) {

            @JavascriptInterface
            fun onVideoReady() {
                Logger.debug("Video loaded")
            }
        }

        init {
            binding.textBoxName.setDrawableCompat(start = IconUtils.boxIcon(buildContext) {
                copy(sizeDp = 12)
            })
            binding.webView.addJavascriptInterface(
                YoutubeJsInterface(mainHandler, binding), "android"
            )
            binding.webView.settings.apply {
                javaScriptEnabled = true
            }
            binding.webView.webViewClient = object : WebViewClientCompat() {
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

        override fun onBind(model: ListYoutubeVideoModelView, position: Int) {
            val html = buildContext.assets.open("youtube_embed/embed.html").reader().readText()
            val formatHtml = String.format(html, model.videoId)
            val encodeHtml = Base64.encodeToString(formatHtml.toByteArray(), Base64.NO_PADDING)
            binding.webView.loadData(encodeHtml, "text/html", "base64")

            binding.bottomAction.setBookmarkIcon(
                model.shareDetail.bookmarked.asBookmarkIcon(buildContext)
            )

            binding.bottomAction.setLikeIcon(model.shareDetail.liked.asLikeIcon(buildContext))

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

            ImageLoader.INSTANCE.load(
                buildContext, model.shareDetail.user.avatar, binding.layoutUserInfo.imageAvatar
            ) {
                copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
            }

            binding.layoutUserInfo.textViewName.text = buildSpannedString {
                bold {
                    append(model.shareDetail.user.name)
                }
                append(buildContext.getString(R.string.share_video))
            }
            binding.layoutUserInfo.textUserLevel.text =
                buildContext.getString(
                    R.string.user_level_format,
                    UserUtils.getLevelTitle(model.shareDetail.user.level),
                    model.shareDetail.shareDate.asElapsedTimeDisplay()
                )

            binding.textBoxName.text =
                model.shareDetail.boxDetail?.boxName ?: buildContext.getText(R.string.box_community)

            model.shareDetail.shareNote.takeIfNotNullOrBlank()?.let { text ->
                binding.textViewNote.isVisible = true
                binding.textViewNote.setReadMoreText(text)
            } ?: binding.textViewNote.apply {
                text = null
                isVisible = false
            }
        }

        override fun onUnBind() {
            binding.webView.loadUrl("auto:blank")
            binding.textViewNote.text = null
            binding.bottomAction.release()
            ImageLoader.INSTANCE.release(buildContext, binding.layoutUserInfo.imageAvatar)
        }
    }
}
