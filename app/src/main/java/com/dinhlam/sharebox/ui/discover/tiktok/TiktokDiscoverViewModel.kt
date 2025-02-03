package com.dinhlam.sharebox.ui.discover.tiktok

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.network.TiktokServices
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.extensions.ifTrue
import com.dinhlam.sharebox.extensions.toggleElement
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.TiktokCategory
import com.dinhlam.sharebox.model.TiktokDiscover
import com.dinhlam.sharebox.utils.UserAgentUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TiktokDiscoverViewModel @Inject constructor(
    private val tiktokServices: TiktokServices,
    private val boxRepository: BoxRepository,
    private val userHelper: UserHelper,
    private val shareRepository: ShareRepository
) : BaseViewModel<TiktokDiscoverState>(TiktokDiscoverState()) {

    init {
        getDefaultBox()
        onChange(TiktokDiscoverState::activeCategories, ::getTiktokTrending)
    }

    private fun getDefaultBox() {
        suspend {
            boxRepository.findFirst(userHelper.getCurrentUserId())
        }.execute { asyncLoad ->
            copy(currentBox = asyncLoad.data)
        }
    }

    fun refresh() {
        setState { copy(cache = emptyMap()) }
        getState { state ->
            getTiktokTrending(state.activeCategories)
        }
    }

    private fun getTiktokTrending(categories: Set<TiktokCategory>) = getState { state ->
        suspend {
            buildList {
                categories.forEach { tiktokCategory ->
                    val cacheData = state.cache[tiktokCategory.categoryId]
                    if (cacheData != null) {
                        add(tiktokCategory.categoryId to cacheData)
                    } else {
                        val queryMap = mapOf(
                            "count" to "20",
                            "categoryType" to "${tiktokCategory.categoryId}",
                            "aid" to "1988",
                            "app_language" to "en",
                            "app_name" to "tiktok_web"
                        )
                        val list = tiktokServices.explore(
                            UserAgentUtils.pickRandomUserAgent(),
                            queryMap
                        ).itemList.map { item ->
                            TiktokDiscover(
                                item.id,
                                "https://www.tiktok.com/@${item.author.uniqueId}/video/${item.video.id}",
                                item.stats.playCount,
                                item.desc
                            )
                        }
                        add(tiktokCategory.categoryId to list)
                    }
                }
            }.shuffled()
        }.execute { asyncLoad ->
            copy(
                asyncLoadTiktokDiscover = asyncLoad,
                tiktokDiscoverList = asyncLoad.completed.ifTrue(
                    asyncLoad.data.orEmpty().map(Pair<Int, List<TiktokDiscover>>::second).flatten()
                        .shuffled(),
                    tiktokDiscoverList
                ),
                cache = cache.plus(asyncLoad.data.orEmpty())
            )
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

    fun setActiveCategory(tiktokCategory: TiktokCategory) = getState { state ->
        if (state.activeCategories.contains(tiktokCategory) && state.activeCategories.size == 1) {
            return@getState
        }
        setState { copy(activeCategories = activeCategories.toggleElement(tiktokCategory)) }
    }
}