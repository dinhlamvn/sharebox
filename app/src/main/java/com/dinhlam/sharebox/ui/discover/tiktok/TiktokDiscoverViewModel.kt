package com.dinhlam.sharebox.ui.discover.tiktok

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.network.TiktokServices
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.extensions.ifTrue
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
        onChange(TiktokDiscoverState::activeCategory) { tiktokCategory ->
            val categoryId = tiktokCategory?.categoryId ?: return@onChange
            getTiktokTrending(categoryId)
        }
    }

    private fun getDefaultBox() {
        suspend {
            boxRepository.findFirst(userHelper.getCurrentUserId())
        }.execute { asyncLoad ->
            copy(currentBox = asyncLoad.data)
        }
    }

    fun refresh() = getState { state ->
        val categoryId = state.activeCategory?.categoryId ?: return@getState
        getTiktokTrending(categoryId)
    }

    private fun getTiktokTrending(categoryId: Int) = suspend {
        val queryMap = mapOf(
            "count" to "20",
            "categoryType" to "$categoryId",
            "aid" to "1988",
            "app_language" to "en",
            "app_name" to "tiktok_web"
        )
        tiktokServices.explore(
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
    }.execute { asyncLoad ->
        copy(
            asyncLoadTiktokDiscover = asyncLoad,
            tiktokDiscoverList = asyncLoad.completed.ifTrue(asyncLoad.data, tiktokDiscoverList)
                .orEmpty()
        )
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

    fun setActiveCategory(tiktokCategory: TiktokCategory) {
        setState { copy(activeCategory = tiktokCategory) }
    }
}