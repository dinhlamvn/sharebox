package com.dinhlam.sharebox.ui.discover.tiktok

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.network.TiktokServices
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.downloader.Downloader
import com.dinhlam.sharebox.extensions.ifTrue
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.TiktokCategory
import com.dinhlam.sharebox.model.TiktokDiscover
import com.dinhlam.sharebox.utils.BoxUtils
import com.dinhlam.sharebox.utils.UserAgentUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class TiktokDiscoverViewModel @Inject constructor(
    private val tiktokServices: TiktokServices,
    private val boxRepository: BoxRepository,
    private val userHelper: UserHelper,
    private val shareRepository: ShareRepository,
    @Named("TiktokDownloaderV2") private val tiktokDownloader: Downloader
) : BaseViewModel<TiktokDiscoverState>(TiktokDiscoverState()) {

    init {
        getDefaultBox()
        onChange(TiktokDiscoverState::activeCategory, ::getTiktokTrending)
    }

    private fun getDefaultBox() {
        suspend {
            val boxId = BoxUtils.createBoxId("${userHelper.getCurrentUserId()}-tiktok-box")
            boxRepository.findOne(boxId) ?: boxRepository.run {
                boxRepository.insert(boxId, "Tiktok Discover", null, userHelper.getCurrentUserId())
                boxRepository.findOne(boxId)
            }
        }.execute { asyncLoad ->
            copy(currentBox = asyncLoad.data)
        }
    }

    fun refresh() {
        getState { state ->
            getTiktokTrending(state.activeCategory)
        }
    }

    private fun getTiktokTrending(category: TiktokCategory) {
        suspend {
            getDiscoverList(category)
        }.execute { asyncLoad ->
            copy(
                asyncLoadTiktokDiscover = asyncLoad,
                tiktokDiscoverList = asyncLoad.completed.ifTrue(
                    asyncLoad.data.orEmpty(),
                    tiktokDiscoverList
                ),
            )
        }
    }

    private suspend fun getDiscoverList(
        category: TiktokCategory
    ): List<TiktokDiscover> {
        val queryMap = mapOf(
            "count" to "20",
            "categoryType" to "${category.categoryId}",
            "aid" to "1988",
            "app_language" to "en",
            "app_name" to "tiktok_web"
        )
        return tiktokServices.explore(
            UserAgentUtils.pickRandomUserAgent(),
            queryMap
        ).itemList.map { item ->
            TiktokDiscover(
                item.id,
                "https://www.tiktok.com/@${item.author.uniqueId}/video/${item.video.id}",
                item.stats.playCount,
                item.stats.commentCount,
                item.stats.diggCount,
                item.stats.shareCount,
                item.desc
            )
        }
    }

    fun archiveLink(link: String, note: String?, boxId: String) {
        suspend {
            val shareData = ShareData.ShareUrl(link)
            shareRepository.insert(
                shareData = shareData,
                shareNote = note,
                shareBoxId = boxId,
                shareUserId = userHelper.getCurrentUserId(),
                isVideoShare = true
            )
            link
        }.execute { asyncLoad ->
            copy(asyncLoadArchive = asyncLoad)
        }
    }

    fun setActiveCategory(tiktokCategory: TiktokCategory) {
        setState { copy(activeCategory = tiktokCategory) }
    }

    fun loadTiktokVideo(tiktokDiscover: TiktokDiscover, block: (String?) -> Unit) =
        getState { state ->
            val cachedUrl = state.tiktokDownloadUrlCache[tiktokDiscover.url]
            if (cachedUrl != null) {
                block(cachedUrl)
            } else {
                doInBackground {
                    val downloadContent = tiktokDownloader.download(tiktokDiscover.url)
                    val videoUrl = downloadContent.videos.firstOrNull()?.downloadUrl
                    withContext(Dispatchers.Main) {
                        block(videoUrl)
                        if (videoUrl != null) {
                            setState {
                                copy(
                                    tiktokDownloadUrlCache = tiktokDownloadUrlCache.plus(
                                        tiktokDiscover.url to videoUrl
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

    fun loadMore() = getState { state ->
        suspend {
            getDiscoverList(state.activeCategory)
        }.execute { asyncLoad ->
            if (asyncLoad is AsyncLoad.Success) {
                val list = asyncLoad.value
                val newList = tiktokDiscoverList.plus(list).distinct()
                copy(tiktokDiscoverList = newList, isLoadingMore = false)
            } else {
                copy(isLoadingMore = asyncLoad is AsyncLoad.Loading)
            }
        }
    }
}