package com.dinhlam.sharebox.ui.discover.zingnews

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.network.DownloadServices
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.extensions.toggleElement
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ZingNewsCategory
import com.dinhlam.sharebox.model.ZingNewsDiscover
import com.dinhlam.sharebox.utils.BoxUtils
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
        onChange(ZingNewsDiscoverState::zingNewsCheckedCategories, ::getZingNewsData)
        setDefaultCategory()
    }

    private fun setDefaultCategory() = setState {
        val categories = ZingNewsCategory.categories.copyOf().toMutableList()
        categories.shuffle()
        copy(zingNewsCategories = categories, zingNewsCheckedCategories = setOf(categories.first()))
    }

    private fun getDefaultBox() {
        suspend {
            val boxId = BoxUtils.createBoxId("${userHelper.getCurrentUserId()}-zingnews-box")
            boxRepository.findOne(boxId) ?: boxRepository.run {
                boxRepository.insert(boxId, "ZingNews Discover", null, userHelper.getCurrentUserId())
                boxRepository.findOne(boxId)
            }
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

    private fun getZingNewsData(categories: Set<ZingNewsCategory>) = getState { state ->
        suspend {
            buildList {
                categories.forEach { zingNewsCategory ->
                    val url = zingNewsCategory.url
                    val cachedData = state.cache[url]
                    if (cachedData != null) {
                        add(url to cachedData)
                    } else {
                        val responseBody =
                            downloadServices.downloadFileWithoutStream(zingNewsCategory.url)
                        responseBody.use { body ->
                            val html = body.string()
                            val jsoup = Jsoup.parse(html)
                            val list = getDataFromHTML(jsoup)
                            add(url to list)
                        }
                    }
                }
            }
        }.execute { asyncLoad ->
            copy(
                asyncLoadZingNewsDiscover = asyncLoad,
                zingNewsDiscovers = asyncLoad.data?.map(Pair<String, List<ZingNewsDiscover>>::second)
                    ?.flatten()?.shuffled() ?: zingNewsDiscovers,
                cache = cache.plus(asyncLoad.data.orEmpty())
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

    fun setActiveCategory(zingNewsCategory: ZingNewsCategory) = getState { state ->
        if (state.zingNewsCheckedCategories.contains(zingNewsCategory) && state.zingNewsCheckedCategories.size == 1) {
            return@getState
        }
        setState {
            copy(
                zingNewsCheckedCategories = zingNewsCheckedCategories.toggleElement(zingNewsCategory)
            )
        }
    }

    fun refresh() {
        setState { copy(cache = emptyMap()) }
        getState { state ->
            getZingNewsData(state.zingNewsCheckedCategories)
        }
    }
}