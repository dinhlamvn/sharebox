package com.dinhlam.sharebox.ui.home.videomixer.modelview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.core.view.isVisible
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.data.model.ShareDetail
import com.dinhlam.sharebox.databinding.ModelViewVideoFacebookBinding
import com.dinhlam.sharebox.extensions.asBookmarkIcon
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType

data class FacebookVideoModelView(
    val id: String,
    val videoId: String,
    val shareDetail: ShareDetail,
    val actionShareToOther: BaseListAdapter.NoHashProp<Function1<String, Unit>> = BaseListAdapter.NoHashProp(
        null
    ),
    val actionVote: BaseListAdapter.NoHashProp<Function1<String, Unit>> = BaseListAdapter.NoHashProp(
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
        return FacebookVideoViewHolder(
            ModelViewVideoFacebookBinding.inflate(
                inflater, container, false
            )
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    private class FacebookVideoViewHolder(binding: ModelViewVideoFacebookBinding) :
        BaseListAdapter.BaseViewHolder<FacebookVideoModelView, ModelViewVideoFacebookBinding>(
            binding
        ) {

        private val webViewAssetLoader by lazy {
            WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(buildContext))
                .addPathHandler("/res/", WebViewAssetLoader.ResourcesPathHandler(buildContext))
                .build()
        }

        init {
            binding.bottomAction.apply {
                setVoteIcon(R.drawable.ic_arrow_up_white)
                setCommentIcon(R.drawable.ic_comment_white)
                setShareIcon(R.drawable.ic_share_white)
                setVoteTextColor(Color.WHITE)
                setCommentTextColor(Color.WHITE)
            }
            binding.webView.setBackgroundColor(Color.BLACK)
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

        override fun onBind(model: FacebookVideoModelView, position: Int) {
            val html = buildContext.assets.open("fb_embed/embed.html").reader().readText()
            val formatHtml = String.format(html, model.videoId)
            val encodeHtml = Base64.encodeToString(formatHtml.toByteArray(), Base64.NO_PADDING)
            binding.webView.loadData(encodeHtml, "text/html", "base64")

            binding.bottomAction.setBookmarkIcon(
                model.shareDetail.bookmarked.asBookmarkIcon(
                    R.drawable.ic_bookmarked_white,
                    R.drawable.ic_bookmark_white
                )
            )

            binding.bottomAction.setOnShareClickListener {
                model.actionShareToOther.prop?.invoke(model.shareDetail.shareId)
            }

            binding.bottomAction.setOnCommentClickListener {
                model.actionComment.prop?.invoke(model.shareDetail.shareId)
            }

            binding.bottomAction.setOnLikeClickListener {
                model.actionVote.prop?.invoke(model.shareDetail.shareId)
            }

            binding.bottomAction.setOnBookmarkClickListener {
                model.actionBookmark.prop?.invoke(model.shareDetail.shareId)
            }

            binding.bottomAction.setVoteNumber(model.shareDetail.voteCount)
            binding.bottomAction.setCommentNumber(model.shareDetail.commentCount)

            binding.textViewName.text = model.shareDetail.user.name
            ImageLoader.instance.load(
                buildContext,
                model.shareDetail.user.avatar,
                binding.imageAvatar
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
            ImageLoader.instance.release(buildContext, binding.imageAvatar)
        }
    }
}
