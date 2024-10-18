package com.dinhlam.sharebox.ui.discover.zingnews

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.network.DownloadServices
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ZingNewsCategory
import com.dinhlam.sharebox.model.ZingNewsDiscover
import dagger.hilt.android.lifecycle.HiltViewModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import javax.inject.Inject

@HiltViewModel
class ZingNewDiscoverViewModel @Inject constructor(
    private val downloadServices: DownloadServices,
    private val boxRepository: BoxRepository,
    private val userHelper: UserHelper,
    private val shareRepository: ShareRepository,
) : BaseViewModel<ZingNewsDiscoverState>(ZingNewsDiscoverState()) {

    init {
        getDefaultBox()
        onChange(ZingNewsDiscoverState::zingNewsCategory) { zingNewsCategory ->
            zingNewsCategory?.url?.let(::getZingNewsData)
        }
        setDefaultCategory()
    }

    private fun setDefaultCategory() = setState {
        val categories = ZingNewsCategory.categories.copyOf().toMutableList()
        categories.shuffle()
        copy(zingNewsCategories = categories, zingNewsCategory = categories.firstOrNull())
    }

    private fun getDefaultBox() {
        suspend {
            boxRepository.findFirst(userHelper.getCurrentUserId())
        }.execute { asyncLoad ->
            copy(currentBox = asyncLoad.data)
        }
    }

    fun setCurrentBoxId(boxId: String) {
        suspend { boxRepository.findOne(boxId) }.execute { asyncLoad ->
            copy(currentBox = asyncLoad.data)
        }
    }

    fun archiveLink(link: String, boxId: String) {
        suspend {
            val shareData = ShareData.ShareUrl(link)
            shareRepository.insert(
                shareData = shareData,
                shareNote = null,
                shareBoxId = boxId,
                shareUserId = userHelper.getCurrentUserId(),
                isVideoShare = true
            )
            link
        }.execute { asyncLoad ->
            copy(asyncLoadArchive = asyncLoad)
        }
    }

    private fun getZingNewsData(url: String) {
        suspend {
            val responseBody =
                downloadServices.downloadFileWithoutStream(url)
            responseBody.use { body ->
                val html = body.string()
                val jsoup = Jsoup.parse(html)
                getDataFromHTML(jsoup)
            }
        }.execute { asyncLoad ->
            copy(
                asyncLoadZingNewsDiscover = asyncLoad,
                zingNewsDiscovers = asyncLoad.data.orEmpty()
            )
        }
    }

    private fun getDataFromHTML(document: Document): List<ZingNewsDiscover> {
        val articles = document.getElementsByClass("article-title")
        return articles.map { article ->
            val aTag = article.getElementsByTag("a")[0]
            val url = aTag.attr("href")
            val title = aTag.text()
            ZingNewsDiscover(title, url)
        }
    }

    fun setActiveCategory(zingNewsCategory: ZingNewsCategory) {
        setState { copy(zingNewsCategory = zingNewsCategory) }
    }
}