package com.dinhlam.sharebox.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.databinding.ViewShareBoxLinkPreviewBinding
import com.dinhlam.sharebox.extensions.trimToDomain
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.utils.LinkPreviewCacheUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.util.concurrent.Executors

class ShareBoxLinkPreviewView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    companion object {
        private const val REFERRER = "http://www.google.com"
        private const val TIMEOUT = 100000
        private const val DOC_SELECT_OG_TAGS = "meta[property^=og:]"
        private const val DOC_SELECT_DESCRIPTION = "meta[name=description]"
        private const val OPEN_GRAPH_KEY = "content"
        private const val PROPERTY = "property"
        private const val OG_IMAGE = "og:image"
        private const val OG_DESCRIPTION = "og:description"
        private const val OG_URL = "og:url"
        private const val OG_TITLE = "og:title"
        private const val OG_SITE_NAME = "og:site_name"
        private const val OG_TYPE = "og:type"

        private val AGENTS = arrayOf(
            "facebookexternalhit/1.1 (+http://www.facebook.com/externalhit_uatext.php)",
            "Mozilla",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Safari/537.36",
            "WhatsApp/2.19.81 A",
            "facebookexternalhit/1.1",
            "facebookcatalog/1.0"
        )
    }

    private val job = Job()

    private val linkPreviewScope =
        CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher() + job)

    data class OpenGraphResult(
        var title: String? = null,
        var description: String? = null,
        var url: String? = null,
        var image: String? = null,
        var siteName: String? = null,
        var type: String? = null
    )

    private val binding: ViewShareBoxLinkPreviewBinding = ViewShareBoxLinkPreviewBinding.inflate(
        LayoutInflater.from(context), this
    )

    private fun resetUi() {
        binding.textViewUrl.hint = null
        binding.textViewTitle.text = null
        binding.imageView.setImageDrawable(null)
        binding.frameLoading.isVisible = true
    }

    fun setLink(url: String?) {
        url?.let { nonNullUrl ->
            resetUi()
            binding.frameLoading.isVisible = true
            linkPreviewScope.launch {
                AGENTS.forEach { agent ->
                    val openGraphResult =
                        LinkPreviewCacheUtils.getCache(nonNullUrl) ?: getLinkInfo(nonNullUrl, agent)
                    openGraphResult?.let { nonNullResult ->
                        LinkPreviewCacheUtils.setCache(nonNullUrl, nonNullResult)
                        handleResult(nonNullResult)
                        return@launch
                    }
                }

                handleErrorResult(nonNullUrl)
            }
        }
    }

    private suspend fun handleResult(openGraphResult: OpenGraphResult) =
        withContext(Dispatchers.Main) {
            ImageLoader.INSTANCE.load(
                context,
                openGraphResult.image,
                binding.imageView
            ) {
                copy(transformType = TransformType.Normal(ImageLoadScaleType.CenterCrop))
            }
            binding.textViewUrl.hint = openGraphResult.url?.trimToDomain()
            binding.textViewTitle.text = openGraphResult.title
            binding.frameLoading.isVisible = false
        }

    private suspend fun handleErrorResult(url: String) = withContext(Dispatchers.Main) {
        ImageLoader.INSTANCE.load(context, R.drawable.image_no_preview, binding.imageView)
        binding.textViewUrl.hint = url
        binding.frameLoading.isVisible = false
    }

    private fun getLinkInfo(url: String, agent: String): OpenGraphResult? {
        val openGraphResult = OpenGraphResult()
        return try {
            val response =
                Jsoup.connect(url).ignoreContentType(true).userAgent(agent).referrer(REFERRER)
                    .timeout(TIMEOUT).followRedirects(true).execute()

            val doc = response.parse()
            val ogTags = doc.select(DOC_SELECT_OG_TAGS)

            ogTags.forEach { tag ->
                when (tag.attr(PROPERTY)) {
                    OG_IMAGE -> {
                        openGraphResult.image = tag.attr(OPEN_GRAPH_KEY)
                    }

                    OG_DESCRIPTION -> {
                        openGraphResult.description = tag.attr(OPEN_GRAPH_KEY)
                    }

                    OG_URL -> {
                        openGraphResult.url = tag.attr(OPEN_GRAPH_KEY)
                    }

                    OG_TITLE -> {
                        openGraphResult.title = tag.attr(OPEN_GRAPH_KEY)
                    }

                    OG_SITE_NAME -> {
                        openGraphResult.siteName = tag.attr(OPEN_GRAPH_KEY)
                    }

                    OG_TYPE -> {
                        openGraphResult.type = tag.attr(OPEN_GRAPH_KEY)
                    }
                }
            }

            if (openGraphResult.title.isNullOrEmpty()) {
                openGraphResult.title = doc.title()
            }
            if (openGraphResult.description.isNullOrEmpty()) {
                val docSelection = doc.select(DOC_SELECT_DESCRIPTION)
                openGraphResult.description = docSelection.firstOrNull()?.attr("content") ?: ""
            }
            if (openGraphResult.url.isNullOrEmpty()) {
                openGraphResult.url = url
            }
            openGraphResult
        } catch (e: Exception) {
            null
        }
    }

    fun release() {
        ImageLoader.INSTANCE.release(context, binding.imageView)
        resetUi()
    }
}